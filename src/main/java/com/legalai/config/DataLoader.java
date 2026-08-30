package com.legalai.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legalai.model.CaseOutcome;
import com.legalai.model.CourtLevel;
import com.legalai.model.LegalCase;
import com.legalai.model.LegalDomain;
import com.legalai.repository.LegalCaseRepository;
import com.legalai.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the Kaggle Legal Judgment benchmark dataset into the Supabase / PostgreSQL database
 * and triggers TF-IDF vector space indexing on startup.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final LegalCaseRepository caseRepository;
    private final RecommendationService recommendationService;
    private final ObjectMapper objectMapper;

    public DataLoader(LegalCaseRepository caseRepository, 
                      RecommendationService recommendationService,
                      ObjectMapper objectMapper) {
        this.caseRepository = caseRepository;
        this.recommendationService = recommendationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        log.info("Synchronizing Kaggle Legal Judgment dataset with database...");

        try {
            ClassPathResource resource = new ClassPathResource("data/kaggle_legal_dataset.json");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    List<LegalCase> jsonCases = objectMapper.readValue(is, new TypeReference<List<LegalCase>>() {});
                    int addedCount = 0;
                    for (LegalCase lc : jsonCases) {
                        if (caseRepository.findByCaseNumber(lc.getCaseNumber()).isEmpty()) {
                            caseRepository.save(lc);
                            addedCount++;
                        }
                    }
                    log.info("Dataset synchronization complete: {} new cases ingested. Total cases in DB: {}.", 
                            addedCount, caseRepository.count());
                }
            }
        } catch (Exception e) {
            log.warn("Could not sync from json dataset file: {}.", e.getMessage());
        }

        if (caseRepository.count() == 0) {
            caseRepository.saveAll(getDefaultBenchmarkCases());
        }

        recommendationService.reindexCorpus();
        log.info("TF-IDF Vector Corpus indexing complete with {} active authorities.", caseRepository.count());
    }

    private List<LegalCase> getDefaultBenchmarkCases() {
        List<LegalCase> cases = new ArrayList<>();

        cases.add(new LegalCase(
                null, "SC-2023-CONST-101", "(2023) 5 SCC 201",
                "Citizens Privacy Forum vs. Union Communications Authority",
                LegalDomain.CONSTITUTIONAL, CourtLevel.SUPREME_COURT,
                "Supreme Court of India", "Constitution Bench (5 Judges)",
                "Justice D.Y. Chandrachud, Justice P.S. Narasimha, Justice S.R. Bhat",
                "Citizens Privacy Forum & Digital Rights Alliance",
                "Union Communications Authority & Ministry of Home Affairs",
                2021, LocalDate.of(2023, 4, 18), 24,
                "The petitioners challenged state executive orders authorizing bulk automated surveillance, biometric telemetry retention, and lawful interception of encrypted communications without prior independent judicial warrant. The state argued national security and prevention of organized cyber terrorism.",
                "Whether bulk automated metadata interception without prior judicial warrant violates the fundamental right to privacy under Article 21 and the principle of proportionality.",
                "Article 21 Constitution, Section 69 Information Technology Act, Article 19(1)(a)",
                "K.S. Puttaswamy v. Union of India (2017) 10 SCC 1; Maneka Gandhi v. Union of India (1978)",
                "The Apex Court struck down warrantless mass interception. Held that state surveillance must strictly satisfy the 4-pronged proportionality test: legitimate state aim, suitability, necessity, and strict balancing. Independent judicial oversight is mandatory for biometric interception.",
                CaseOutcome.PETITIONER_FAVOR, "Executive Orders Quashed; Statutory Judicial Oversight Mandated",
                null, true, "Right to Privacy, Article 21, Biometrics, Surveillance, Proportionality", 1420, 88
        ));

        return cases;
    }
}
