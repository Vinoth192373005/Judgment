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

    @BeforeEach
    void setUp() {
        if (caseRepository.count() == 0) {
            LegalCase c1 = new LegalCase();
            c1.setCaseNumber("TEST-PRIVACY-01");
            c1.setCitation("2023 SCC 101");
            c1.setTitle("Privacy Forum vs. State");
            c1.setDomain(LegalDomain.CONSTITUTIONAL);
            c1.setCourtLevel(CourtLevel.SUPREME_COURT);
            c1.setCourtName("Supreme Court");
            c1.setPetitioner("Privacy Forum");
            c1.setRespondent("State");
            c1.setFilingYear(2023);
            c1.setJudgmentDate(LocalDate.of(2023, 5, 1));
            c1.setFactsSynopsis("Biometric privacy and fundamental rights.");
            c1.setLegalIssues("Right to privacy under Article 21.");
            c1.setStatutesCited("Article 21");
            c1.setRatioDecidendi("Privacy is a fundamental right.");
            c1.setOutcome(CaseOutcome.PETITIONER_FAVOR);
            c1.setLandmarkCase(true);
            caseRepository.save(c1);
        }
    }

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
        assertFalse(results.isEmpty(), "Should find privacy cases in repository");
    }

    @Test
    void testLandmarkCasesFilter() {
        List<LegalCase> landmarks = caseRepositoryService.getLandmarkCases();
        assertNotNull(landmarks);
        assertFalse(landmarks.isEmpty());
        assertTrue(landmarks.stream().allMatch(LegalCase::isLandmarkCase));
    }
}
