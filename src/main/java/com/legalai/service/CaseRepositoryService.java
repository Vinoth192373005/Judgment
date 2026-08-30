package com.legalai.service;

import com.legalai.model.CaseSearchCriteria;
import com.legalai.model.LegalCase;
import com.legalai.model.LegalDomain;
import com.legalai.repository.LegalCaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service managing Case Repository CRUD, advanced search queries, and repository state.
 */
@Service
public class CaseRepositoryService {

    private final LegalCaseRepository caseRepository;

    public CaseRepositoryService(LegalCaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    public List<LegalCase> getAllCases() {
        return caseRepository.findAll();
    }

    public Optional<LegalCase> getCaseById(Long id) {
        Optional<LegalCase> optionalCase = caseRepository.findById(id);
        optionalCase.ifPresent(this::incrementViewCount);
        return optionalCase;
    }

    public Optional<LegalCase> getCaseByCaseNumber(String caseNumber) {
        return caseRepository.findByCaseNumber(caseNumber);
    }

    public List<LegalCase> searchCases(CaseSearchCriteria criteria) {
        if (criteria == null) {
            return caseRepository.findAll();
        }
        return caseRepository.searchCases(
                criteria.getQuery(),
                criteria.getDomain(),
                criteria.getCourtLevel(),
                criteria.getOutcome(),
                criteria.getLandmarkOnly(),
                criteria.getStartYear(),
                criteria.getEndYear(),
                criteria.getStatute(),
                criteria.getJudge()
        );
    }

    public List<LegalCase> getLandmarkCases() {
        return caseRepository.findByLandmarkCaseTrue();
    }

    public List<LegalCase> getCasesByDomain(LegalDomain domain) {
        return caseRepository.findByDomain(domain);
    }

    @Transactional
    public LegalCase createCase(LegalCase legalCase) {
        if (legalCase.getJudgmentDate() != null && legalCase.getFilingYear() == 0) {
            legalCase.setFilingYear(legalCase.getJudgmentDate().getYear());
        }
        if (legalCase.getJudgmentDate() == null) {
            legalCase.setJudgmentDate(LocalDate.now());
            legalCase.setFilingYear(LocalDate.now().getYear());
        }
        return caseRepository.save(legalCase);
    }

    @Transactional
    public LegalCase updateCase(Long id, LegalCase updatedCase) {
        return caseRepository.findById(id).map(existing -> {
            existing.setCaseNumber(updatedCase.getCaseNumber());
            existing.setCitation(updatedCase.getCitation());
            existing.setTitle(updatedCase.getTitle());
            existing.setDomain(updatedCase.getDomain());
            existing.setCourtLevel(updatedCase.getCourtLevel());
            existing.setCourtName(updatedCase.getCourtName());
            existing.setBenchType(updatedCase.getBenchType());
            existing.setPresidingJudges(updatedCase.getPresidingJudges());
            existing.setPetitioner(updatedCase.getPetitioner());
            existing.setRespondent(updatedCase.getRespondent());
            existing.setFilingYear(updatedCase.getFilingYear());
            existing.setJudgmentDate(updatedCase.getJudgmentDate());
            existing.setCaseDurationMonths(updatedCase.getCaseDurationMonths());
            existing.setFactsSynopsis(updatedCase.getFactsSynopsis());
            existing.setLegalIssues(updatedCase.getLegalIssues());
            existing.setStatutesCited(updatedCase.getStatutesCited());
            existing.setPrecedentsCited(updatedCase.getPrecedentsCited());
            existing.setRatioDecidendi(updatedCase.getRatioDecidendi());
            existing.setOutcome(updatedCase.getOutcome());
            existing.setSentenceOrDamages(updatedCase.getSentenceOrDamages());
            existing.setDamagesAmount(updatedCase.getDamagesAmount());
            existing.setLandmarkCase(updatedCase.isLandmarkCase());
            existing.setKeyTags(updatedCase.getKeyTags());
            return caseRepository.save(existing);
        }).orElseThrow(() -> new IllegalArgumentException("Case with ID " + id + " not found."));
    }

    @Transactional
    public void deleteCase(Long id) {
        if (!caseRepository.existsById(id)) {
            throw new IllegalArgumentException("Case with ID " + id + " not found.");
        }
        caseRepository.deleteById(id);
    }

    @Transactional
    public void incrementViewCount(LegalCase legalCase) {
        legalCase.setViewCount(legalCase.getViewCount() + 1);
        caseRepository.save(legalCase);
    }

    @Transactional
    public List<LegalCase> importCases(List<LegalCase> cases) {
        return caseRepository.saveAll(cases);
    }

    public long getTotalCaseCount() {
        return caseRepository.count();
    }
}
