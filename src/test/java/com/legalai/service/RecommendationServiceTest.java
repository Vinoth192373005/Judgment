package com.legalai.service;

import com.legalai.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RecommendationServiceTest {

    @Autowired
    private RecommendationService recommendationService;

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
        assertNotNull(topMatch.getLegalCase());

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
        request.setFactsSynopsis("A tech startup trained a text-to-image machine learning diffusion model by downloading millions of copyrighted artworks from digital galleries without licensing.");
        request.setDomain(LegalDomain.INTELLECTUAL_PROPERTY);
        request.setStatutes(Collections.singletonList("17 U.S. Code § 107"));
        request.setTopK(3);

        RecommendationResult result = recommendationService.recommend(request);
        assertNotNull(result);
        assertFalse(result.getTopPrecedents().isEmpty());

        // Verify neural net / copyright case was matched
        boolean foundIpCase = result.getTopPrecedents().stream()
                .anyMatch(p -> p.getLegalCase().getDomain() == LegalDomain.INTELLECTUAL_PROPERTY);
        assertTrue(foundIpCase, "Should match intellectual property AI precedent");
    }
}
