package com.legalai.service;

import com.legalai.model.CaseOutcome;
import com.legalai.model.CaseSearchCriteria;
import com.legalai.model.CourtLevel;
import com.legalai.model.LegalCase;
import com.legalai.model.LegalDomain;
import com.legalai.repository.LegalCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CaseRepositoryServiceTest {

    @Autowired
    private CaseRepositoryService caseRepositoryService;

    @Autowired
    private LegalCaseRepository caseRepository;

    @Test
    void testCreateAndRetrieveCase() {
        LegalCase testCase = new LegalCase();
        testCase.setCaseNumber("TEST-2025-001");
        testCase.setCitation("2025 TEST SC 99");
        testCase.setTitle("Test Petitioner vs. Test Respondent");
        testCase.setDomain(LegalDomain.CRIMINAL);
        testCase.setCourtLevel(CourtLevel.SUPREME_COURT);
        testCase.setCourtName("Apex Test Court");
        testCase.setPetitioner("Test Petitioner");
        testCase.setRespondent("Test Respondent");
        testCase.setFilingYear(2025);
        testCase.setJudgmentDate(LocalDate.of(2025, 1, 15));
        testCase.setFactsSynopsis("Test facts regarding digital forgery.");
        testCase.setLegalIssues("Whether digital forgery is proven.");
        testCase.setStatutesCited("Section 465 IPC");
        testCase.setRatioDecidendi("Digital forgery requires cryptographic authentication failure.");
        testCase.setOutcome(CaseOutcome.PETITIONER_FAVOR);
        testCase.setLandmarkCase(true);

        LegalCase created = caseRepositoryService.createCase(testCase);
        assertNotNull(created.getId());

        Optional<LegalCase> retrieved = caseRepositoryService.getCaseById(created.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("TEST-2025-001", retrieved.get().getCaseNumber());
        assertEquals(LegalDomain.CRIMINAL, retrieved.get().getDomain());
    }

    @Test
    void testSearchCases() {
        CaseSearchCriteria criteria = new CaseSearchCriteria();
        criteria.setQuery("privacy");

        List<LegalCase> results = caseRepositoryService.searchCases(criteria);
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find pre-loaded privacy cases");
    }

    @Test
    void testLandmarkCasesFilter() {
        List<LegalCase> landmarks = caseRepositoryService.getLandmarkCases();
        assertNotNull(landmarks);
        assertFalse(landmarks.isEmpty());
        assertTrue(landmarks.stream().allMatch(LegalCase::isLandmarkCase));
    }
}
