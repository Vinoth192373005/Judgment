package com.legalai.service;

import com.legalai.model.AnalyticsSummary;
import com.legalai.model.CaseComparisonResponse;
import com.legalai.model.LegalCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AnalyticsServiceTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private CaseRepositoryService caseRepositoryService;

    @Test
    void testGetAnalyticsSummary() {
        AnalyticsSummary summary = analyticsService.getAnalyticsSummary();

        assertNotNull(summary);
        assertTrue(summary.getTotalCases() >= 10, "Should have seeded legal cases");
        assertTrue(summary.getLandmarkCasesCount() >= 3);
        assertTrue(summary.getOverallPetitionerWinRate() > 0);
        assertNotNull(summary.getCasesByDomain());
        assertFalse(summary.getCasesByDomain().isEmpty());
        assertNotNull(summary.getCasesByOutcome());
        assertFalse(summary.getCasesByOutcome().isEmpty());
        assertNotNull(summary.getTopStatutes());
        assertNotNull(summary.getJudgeTendencies());
        assertNotNull(summary.getYearlyTrends());
    }

    @Test
    void testCompareCases() {
        List<LegalCase> allCases = caseRepositoryService.getAllCases();
        assertTrue(allCases.size() >= 2);

        List<Long> idsToCompare = Arrays.asList(allCases.get(0).getId(), allCases.get(1).getId());
        CaseComparisonResponse comparison = analyticsService.compareCases(idsToCompare);

        assertNotNull(comparison);
        assertEquals(2, comparison.getCases().size());
        assertNotNull(comparison.getAnalyticalComparison());
    }
}
