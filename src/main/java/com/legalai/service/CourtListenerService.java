package com.legalai.service;

import com.legalai.model.*;
import com.legalai.repository.LegalCaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;

/**
 * Service for querying the CourtListener Search API and importing
 * authoritative case opinions directly into the Supabase database.
 */
@Service
public class CourtListenerService {

    private static final Logger log = LoggerFactory.getLogger(CourtListenerService.class);
    private static final String COURTLISTENER_SEARCH_URL = "https://www.courtlistener.com/api/rest/v4/search/";

    private final LegalCaseRepository caseRepository;
    private final RecommendationService recommendationService;
    private final RestTemplate restTemplate;

    @Value("${courtlistener.api.token:}")
    private String apiToken;

    public CourtListenerService(LegalCaseRepository caseRepository, RecommendationService recommendationService) {
        this.caseRepository = caseRepository;
        this.recommendationService = recommendationService;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Searches opinions on the CourtListener API.
     *
     * @param query Search keywords, case name, or statute
     * @param page  Pagination page number
     * @return CourtListenerSearchResponse
     */
    public CourtListenerSearchResponse searchOpinions(String query, int page) {
        try {
            URI targetUri = UriComponentsBuilder.fromHttpUrl(COURTLISTENER_SEARCH_URL)
                    .queryParam("q", query)
                    .queryParam("type", "o")
                    .queryParam("page", Math.max(1, page))
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("User-Agent", "Judgment-AI-Legal-Platform/1.0");

            if (apiToken != null && !apiToken.isBlank()) {
                headers.set("Authorization", "Token " + apiToken.trim());
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<CourtListenerSearchResponse> response = restTemplate.exchange(
                    targetUri,
                    HttpMethod.GET,
                    entity,
                    CourtListenerSearchResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("CourtListener API query '{}' returned {} results", query, response.getBody().getResults() != null ? response.getBody().getResults().size() : 0);
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("CourtListener API query failed for '{}': {}", query, e.getMessage(), e);
        }

        CourtListenerSearchResponse empty = new CourtListenerSearchResponse();
        empty.setResults(Collections.emptyList());
        return empty;
    }

    /**
     * Converts and imports a CourtListener opinion into the Supabase database.
     *
     * @param dto CourtListener opinion record
     * @return Saved LegalCase
     */
    public LegalCase importCase(CourtListenerDTO dto) {
        if (dto == null || dto.getCaseName() == null || dto.getCaseName().isBlank()) {
            throw new IllegalArgumentException("Invalid CourtListener record");
        }

        String docket = (dto.getDocketNumber() != null && !dto.getDocketNumber().isBlank())
                ? dto.getDocketNumber()
                : "CL-" + (dto.getId() != null ? dto.getId() : System.currentTimeMillis());

        // Check if already exists by docket
        return caseRepository.findByCaseNumber(docket).orElseGet(() -> {
            LegalCase legalCase = new LegalCase();
            legalCase.setCaseNumber(docket);

            String citationStr = (dto.getCitation() != null && !dto.getCitation().isEmpty())
                    ? String.join(", ", dto.getCitation())
                    : (dto.getCourtCitationString() != null ? dto.getCourtCitationString() : docket);
            legalCase.setCitation(citationStr);

            legalCase.setTitle(dto.getCaseName());

            // Extract petitioner & respondent from case name (e.g. "A v. B")
            String caseName = dto.getCaseName();
            if (caseName.contains(" v. ")) {
                String[] parts = caseName.split(" v\\. ", 2);
                legalCase.setPetitioner(parts[0].trim());
                legalCase.setRespondent(parts[1].trim());
            } else if (caseName.contains(" vs. ")) {
                String[] parts = caseName.split(" vs\\. ", 2);
                legalCase.setPetitioner(parts[0].trim());
                legalCase.setRespondent(parts[1].trim());
            } else {
                legalCase.setPetitioner(caseName);
                legalCase.setRespondent("State / Government Entity");
            }

            // Infer Domain & Court Level
            legalCase.setDomain(inferDomain(dto));
            legalCase.setCourtLevel(inferCourtLevel(dto));
            legalCase.setCourtName(dto.getCourtExact() != null ? dto.getCourtExact() : (dto.getCourt() != null ? dto.getCourt() : "US Federal Court"));
            legalCase.setPresidingJudges(dto.getJudge() != null && !dto.getJudge().isBlank() ? dto.getJudge() : "Presiding Judicial Bench");
            legalCase.setBenchType("Federal Appellate Bench");

            // Filing / Judgment Date
            int year = 2023;
            if (dto.getDateFiled() != null && !dto.getDateFiled().isBlank()) {
                try {
                    LocalDate parsedDate = LocalDate.parse(dto.getDateFiled().substring(0, 10));
                    legalCase.setJudgmentDate(parsedDate);
                    year = parsedDate.getYear();
                } catch (Exception ignored) {
                }
            }
            legalCase.setFilingYear(year);
            legalCase.setCaseDurationMonths(18);

            // Clean snippet text
            String rawSnippet = dto.getSnippet();
            String cleanSnippet = (rawSnippet != null && !rawSnippet.isBlank())
                    ? rawSnippet.replaceAll("<[^>]*>", "").trim()
                    : "Authoritative opinion retrieved via CourtListener API repository.";
            legalCase.setFactsSynopsis(cleanSnippet);
            legalCase.setLegalIssues("Questions of law and statutory interpretation concerning: " + dto.getCaseName());
            legalCase.setRatioDecidendi(cleanSnippet);
            legalCase.setStatutesCited(dto.getSuitNature() != null && !dto.getSuitNature().isBlank() ? dto.getSuitNature() : "Federal Judicial Code");
            legalCase.setPrecedentsCited("Supreme Court & Circuit Authorities");
            legalCase.setOutcome(CaseOutcome.PETITIONER_FAVOR);
            legalCase.setSentenceOrDamages("Judicial Decree / Opinion");
            legalCase.setLandmarkCase(true);
            legalCase.setKeyTags("CourtListener, Federal Law, " + (dto.getSuitNature() != null ? dto.getSuitNature() : "Precedent"));

            LegalCase saved = caseRepository.save(legalCase);
            log.info("Imported case '{}' ({}) into Supabase repository.", saved.getTitle(), saved.getCaseNumber());

            // Reindex vector corpus so the new case is immediately searchable
            recommendationService.reindexCorpus();
            return saved;
        });
    }

    private LegalDomain inferDomain(CourtListenerDTO dto) {
        String combined = ((dto.getCaseName() != null ? dto.getCaseName() : "") + " " +
                (dto.getSuitNature() != null ? dto.getSuitNature() : "") + " " +
                (dto.getSnippet() != null ? dto.getSnippet() : "")).toLowerCase();

        if (combined.contains("copyright") || combined.contains("patent") || combined.contains("trademark") || combined.contains("fair use")) {
            return LegalDomain.INTELLECTUAL_PROPERTY;
        } else if (combined.contains("criminal") || combined.contains("murder") || combined.contains("fraud") || combined.contains("wire fraud") || combined.contains("conspiracy")) {
            return LegalDomain.CRIMINAL;
        } else if (combined.contains("environment") || combined.contains("pollution") || combined.contains("clean air") || combined.contains("clean water")) {
            return LegalDomain.ENVIRONMENTAL;
        } else if (combined.contains("tax") || combined.contains("revenue") || combined.contains("internal revenue") || combined.contains("irs")) {
            return LegalDomain.TAX_FINANCIAL;
        } else if (combined.contains("labor") || combined.contains("employment") || combined.contains("wage") || combined.contains("overtime") || combined.contains("worker")) {
            return LegalDomain.LABOR_EMPLOYMENT;
        } else if (combined.contains("constitutional") || combined.contains("first amendment") || combined.contains("fourth amendment") || combined.contains("fourteenth amendment") || combined.contains("due process")) {
            return LegalDomain.CONSTITUTIONAL;
        } else if (combined.contains("cyber") || combined.contains("defamation") || combined.contains("libel") || combined.contains("privacy")) {
            return LegalDomain.CYBER_DEFAMATION;
        } else if (combined.contains("probate") || combined.contains("estate") || combined.contains("divorce") || combined.contains("custody")) {
            return LegalDomain.FAMILY_ESTATE;
        }
        return LegalDomain.CORPORATE_COMMERCIAL;
    }

    private CourtLevel inferCourtLevel(CourtListenerDTO dto) {
        String court = dto.getCourt() != null ? dto.getCourt().toLowerCase() : "";
        if (court.contains("scotus") || court.contains("supreme")) {
            return CourtLevel.SUPREME_COURT;
        } else if (court.contains("cir") || court.contains("app") || court.contains("appeal")) {
            return CourtLevel.APPELLATE_COURT;
        } else if (court.contains("dist") || court.contains("d.") || court.contains("federal")) {
            return CourtLevel.DISTRICT_COURT;
        }
        return CourtLevel.HIGH_COURT;
    }
}
