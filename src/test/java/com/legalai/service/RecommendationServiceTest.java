package com.legalai.service;

import com.legalai.model.*;
import com.legalai.repository.LegalCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RecommendationServiceTest {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private LegalCaseRepository caseRepository;

    @BeforeEach
    void setUp() {
        caseRepository.deleteAll();

        // 1. Constitutional Privacy Landmark Case
        LegalCase privacyCase = new LegalCase();
        privacyCase.setCaseNumber("SC-2017-CONST-009");
        privacyCase.setCitation("(2017) 10 SCC 1");
        privacyCase.setTitle("Justice K.S. Puttaswamy (Retd.) vs. Union of India");
        privacyCase.setDomain(LegalDomain.CONSTITUTIONAL);
        privacyCase.setCourtLevel(CourtLevel.SUPREME_COURT);
        privacyCase.setCourtName("Supreme Court of India");
        privacyCase.setPetitioner("Justice K.S. Puttaswamy");
        privacyCase.setRespondent("Union of India");
        privacyCase.setFilingYear(2012);
        privacyCase.setJudgmentDate(LocalDate.of(2017, 8, 24));
        privacyCase.setFactsSynopsis("A retired High Court judge challenged mandatory biometric scheme telemetry and warrantless electronic surveillance. The state argued privacy is not a fundamental right.");
        privacyCase.setLegalIssues("Whether right to privacy is a fundamental right under Article 21 Constitution of India.");
        privacyCase.setStatutesCited("Article 21, Article 19 Constitution of India");
        privacyCase.setRatioDecidendi("The Right to Privacy is a fundamental and inalienable right protected under Article 21.");
        privacyCase.setOutcome(CaseOutcome.PETITIONER_FAVOR);
        privacyCase.setLandmarkCase(true);
        privacyCase.setCitationCount(420);
        caseRepository.save(privacyCase);

        // 2. Criminal Car / Vehicle Theft Case
        LegalCase theftCase = new LegalCase();
        theftCase.setCaseNumber("SC-1957-CRIM-004");
        theftCase.setCitation("AIR 1957 SC 369");
        theftCase.setTitle("K.N. Mehra vs. State of Rajasthan");
        theftCase.setDomain(LegalDomain.CRIMINAL);
        theftCase.setCourtLevel(CourtLevel.SUPREME_COURT);
        theftCase.setCourtName("Supreme Court of India");
        theftCase.setPetitioner("K.N. Mehra & Accused");
        theftCase.setRespondent("State of Rajasthan");
        theftCase.setFilingYear(1952);
        theftCase.setJudgmentDate(LocalDate.of(1957, 2, 11));
        theftCase.setFactsSynopsis("The accused cadets unauthorizedly took off in a military vehicle and aircraft without permission of the commanding officer and fled. Charged with vehicle theft under Section 378 and Section 379 IPC.");
        theftCase.setLegalIssues("Whether unauthorized taking and temporary moving of a vehicle constitutes theft under Section 378 and Section 379 IPC.");
        theftCase.setStatutesCited("Section 378, Section 379 Indian Penal Code, Section 23 IPC");
        theftCase.setRatioDecidendi("Temporary dishonest taking and moving of a vehicle without consent constitutes completed theft under Section 379 IPC.");
        theftCase.setOutcome(CaseOutcome.CONVICTED);
        theftCase.setLandmarkCase(true);
        theftCase.setCitationCount(380);
        caseRepository.save(theftCase);

        // 3. Intellectual Property Copyright Case
        LegalCase ipCase = new LegalCase();
        ipCase.setCaseNumber("SCOTUS-2021-IP-002");
        ipCase.setCitation("141 S. Ct. 1164");
        ipCase.setTitle("Google LLC vs. Oracle America, Inc.");
        ipCase.setDomain(LegalDomain.INTELLECTUAL_PROPERTY);
        ipCase.setCourtLevel(CourtLevel.SUPREME_COURT);
        ipCase.setCourtName("Supreme Court of the United States");
        ipCase.setPetitioner("Google LLC");
        ipCase.setRespondent("Oracle America, Inc.");
        ipCase.setFilingYear(2010);
        ipCase.setJudgmentDate(LocalDate.of(2021, 4, 5));
        ipCase.setFactsSynopsis("Reimplementing software declaring code and API packages for interoperability in mobile smartphone platform without copyright license.");
        ipCase.setLegalIssues("Whether software API declaring code interoperability constitutes statutory Fair Use.");
        ipCase.setStatutesCited("17 U.S. Code § 107");
        ipCase.setRatioDecidendi("Copying declaring code was fair use as a matter of law.");
        ipCase.setOutcome(CaseOutcome.PETITIONER_FAVOR);
        ipCase.setLandmarkCase(true);
        ipCase.setCitationCount(340);
        caseRepository.save(ipCase);

        recommendationService.reindexCorpus();
    }

    @Test
    void testRecommendCriminalCarTheftDoesNotReturnPrivacyCase() {
        RecommendationRequest request = new RecommendationRequest();
        request.setFactsSynopsis("car theft");
        request.setDomain(null); // Auto-infer domain
        request.setTopK(5);

        RecommendationResult result = recommendationService.recommend(request);

        assertNotNull(result);
        assertEquals(LegalDomain.CRIMINAL, result.getInferredDomain(), "Should auto-infer Criminal domain for car theft");
        assertNotNull(result.getTopPrecedents());
        assertFalse(result.getTopPrecedents().isEmpty(), "Should return matching theft precedents");

        // Verify top match is a relevant vehicle/theft precedent and NOT the privacy case
        RecommendationResult.MatchedPrecedent topMatch = result.getTopPrecedents().get(0);
        assertTrue(topMatch.getLegalCase().getTitle().toLowerCase().contains("car") ||
                   topMatch.getLegalCase().getTitle().toLowerCase().contains("theft") ||
                   topMatch.getLegalCase().getTitle().toLowerCase().contains("mehra"),
                   "Top match should be a relevant vehicle/theft precedent");

        // Verify Puttaswamy / Privacy case is NOT returned for car theft
        boolean containsPrivacyCase = result.getTopPrecedents().stream()
                .anyMatch(p -> p.getLegalCase().getTitle().contains("Puttaswamy"));
        assertFalse(containsPrivacyCase, "Privacy case must NOT appear for car theft query");
    }

    @Test
    void testRecommendCasePrecedentsAndOutcome() {
        RecommendationRequest request = new RecommendationRequest();
        request.setFactsSynopsis("Government authorities deployed automated camera facial recognition and biometric telemetry tracking citizens in public spaces without judicial warrant or statutory authorization.");
        request.setLegalIssues("Violation of privacy under constitutional fundamental rights and proportionality standard.");
        request.setDomain(LegalDomain.CONSTITUTIONAL);
        request.setTargetCourtLevel(CourtLevel.HIGH_COURT);
        request.setStatutes(Arrays.asList("Article 21", "Section 69 IT Act"));
        request.setTopK(3);

        RecommendationResult result = recommendationService.recommend(request);

        assertNotNull(result);
        assertNotNull(result.getTopPrecedents());
        assertFalse(result.getTopPrecedents().isEmpty(), "Should return matching precedents");

        RecommendationResult.MatchedPrecedent topMatch = result.getTopPrecedents().get(0);
        assertTrue(topMatch.getOverallScore() > 40.0, "Top match should have significant overall similarity");
        assertEquals(LegalDomain.CONSTITUTIONAL, topMatch.getLegalCase().getDomain());
        assertTrue(topMatch.getLegalCase().getTitle().contains("Puttaswamy"));

        // Check outcome prediction
        PredictionResult prediction = result.getOutcomePrediction();
        assertNotNull(prediction);
        assertNotNull(prediction.getPredictedOutcome());
        assertTrue(prediction.getConfidencePercentage() > 50.0);
        assertNotNull(prediction.getRiskLevel());
        assertNotNull(prediction.getJudicialReasoning());

        // Check arguments
        assertFalse(result.getSuggestedLegalArguments().isEmpty());
    }

    @Test
    void testRecommendCopyrightAI() {
        RecommendationRequest request = new RecommendationRequest();
        request.setFactsSynopsis("A tech startup trained a software model by downloading copyrighted software declaring code and API packages without licensing.");
        request.setDomain(LegalDomain.INTELLECTUAL_PROPERTY);
        request.setStatutes(Collections.singletonList("17 U.S. Code § 107"));
        request.setTopK(3);

        RecommendationResult result = recommendationService.recommend(request);
        assertNotNull(result);
        assertFalse(result.getTopPrecedents().isEmpty());

        // Verify copyright case was matched
        boolean foundIpCase = result.getTopPrecedents().stream()
                .anyMatch(p -> p.getLegalCase().getDomain() == LegalDomain.INTELLECTUAL_PROPERTY);
        assertTrue(foundIpCase, "Should match intellectual property precedent");
    }
}
