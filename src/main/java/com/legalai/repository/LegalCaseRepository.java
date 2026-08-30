package com.legalai.repository;

import com.legalai.model.CaseOutcome;
import com.legalai.model.CourtLevel;
import com.legalai.model.LegalCase;
import com.legalai.model.LegalDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LegalCaseRepository extends JpaRepository<LegalCase, Long> {

    Optional<LegalCase> findByCaseNumber(String caseNumber);

    List<LegalCase> findByDomain(LegalDomain domain);

    List<LegalCase> findByCourtLevel(CourtLevel courtLevel);

    List<LegalCase> findByOutcome(CaseOutcome outcome);

    List<LegalCase> findByLandmarkCaseTrue();

    @Query("SELECT c FROM LegalCase c WHERE " +
            "(:domain IS NULL OR c.domain = :domain) AND " +
            "(:courtLevel IS NULL OR c.courtLevel = :courtLevel) AND " +
            "(:outcome IS NULL OR c.outcome = :outcome) AND " +
            "(:landmarkOnly IS NULL OR :landmarkOnly = false OR c.landmarkCase = true) AND " +
            "(:startYear IS NULL OR c.filingYear >= :startYear) AND " +
            "(:endYear IS NULL OR c.filingYear <= :endYear) AND " +
            "(:statute IS NULL OR LOWER(c.statutesCited) LIKE LOWER(CONCAT('%', :statute, '%'))) AND " +
            "(:judge IS NULL OR LOWER(c.presidingJudges) LIKE LOWER(CONCAT('%', :judge, '%'))) AND " +
            "(:query IS NULL OR (" +
            "LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.caseNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.factsSynopsis) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.legalIssues) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.ratioDecidendi) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.keyTags) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ")) ORDER BY c.judgmentDate DESC")
    List<LegalCase> searchCases(
            @Param("query") String query,
            @Param("domain") LegalDomain domain,
            @Param("courtLevel") CourtLevel courtLevel,
            @Param("outcome") CaseOutcome outcome,
            @Param("landmarkOnly") Boolean landmarkOnly,
            @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear,
            @Param("statute") String statute,
            @Param("judge") String judge
    );

    @Query("SELECT c.domain, COUNT(c) FROM LegalCase c GROUP BY c.domain")
    List<Object[]> countCasesByDomain();

    @Query("SELECT c.outcome, COUNT(c) FROM LegalCase c GROUP BY c.outcome")
    List<Object[]> countCasesByOutcome();

    @Query("SELECT c.courtLevel, COUNT(c) FROM LegalCase c GROUP BY c.courtLevel")
    List<Object[]> countCasesByCourtLevel();

    @Query("SELECT c.filingYear, COUNT(c) FROM LegalCase c GROUP BY c.filingYear ORDER BY c.filingYear ASC")
    List<Object[]> countCasesByYear();

    @Query("SELECT c FROM LegalCase c ORDER BY c.citationCount DESC")
    List<LegalCase> findTopCitedCases();
}
