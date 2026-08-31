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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AnalyticsServiceTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private CaseRepositoryService caseRepositoryService;

    @Autowired
    private LegalCaseRepository caseRepository;

    @BeforeEach
    void setUp() {
        if (caseRepository.count() < 10) {
            LegalDomain[] domains = LegalDomain.values();
            CaseOutcome[] outcomes = new CaseOutcome[]{CaseOutcome.PETITIONER_FAVOR, CaseOutcome.RESPONDENT_FAVOR, CaseOutcome.CONVICTED};
            for (int i = 0; i < 12; i++) {
                LegalCase c = new LegalCase();
                c.setCaseNumber("ANALYTICS-CASE-" + i);
                c.setCitation("202" + (i % 4) + " SCC " + (100 + i));
                c.setTitle("Test Case " + i + " vs. Respondent " + i);
                c.setDomain(domains[i % domains.length]);
                c.setCourtLevel(CourtLevel.SUPREME_COURT);
                c.setCourtName("Supreme Court");
                c.setPetitioner("Petitioner " + i);
                c.setRespondent("Respondent " + i);
                c.setFilingYear(2020 + (i % 4));
                c.setJudgmentDate(LocalDate.of(2020 + (i % 4), 1 + (i % 11), 15));
                c.setCaseDurationMonths(12 + i);
                c.setFactsSynopsis("Facts synopsis for case " + i);
                c.setLegalIssues("Legal issue for case " + i);
                c.setStatutesCited("Statute " + (i % 5));
                c.setRatioDecidendi("Ratio decidendi for case " + i);
                c.setOutcome(outcomes[i % outcomes.length]);
                c.setLandmarkCase(i % 3 == 0);
                c.setDamagesAmount(i % 2 == 0 ? 50000.0 * (i + 1) : null);
                caseRepository.save(c);
            }
        }
    }

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
