package com.legalai.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Core JPA Entity representing a comprehensive legal case record in the repository.
 */
@Entity
@Table(name = "legal_cases")
public class LegalCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Case number cannot be blank")
    @Column(nullable = false, unique = true, length = 100)
    private String caseNumber;

    @Column(length = 150)
    private String citation;

    @NotBlank(message = "Case title cannot be blank")
    @Column(nullable = false, length = 255)
    private String title;

    @NotNull(message = "Legal domain is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LegalDomain domain;

    @NotNull(message = "Court level is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CourtLevel courtLevel;

    @Column(nullable = false, length = 150)
    private String courtName;

    @Column(length = 100)
    private String benchType; // e.g. "Constitution Bench (5 Judges)", "Division Bench", "Single Judge"

    @Column(length = 255)
    private String presidingJudges;

    @Column(nullable = false, length = 200)
    private String petitioner;

    @Column(nullable = false, length = 200)
    private String respondent;

    private int filingYear;

    private LocalDate judgmentDate;

    private int caseDurationMonths;

    @Column(columnDefinition = "TEXT")
    private String factsSynopsis;

    @Column(columnDefinition = "TEXT")
    private String legalIssues;

    @Column(columnDefinition = "TEXT")
    private String statutesCited; // comma-separated or formatted list of statutes

    @Column(columnDefinition = "TEXT")
    private String precedentsCited;

    @Column(columnDefinition = "TEXT")
    private String ratioDecidendi; // Core legal principle / rule of law established

    @NotNull(message = "Outcome is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CaseOutcome outcome;

    @Column(length = 255)
    private String sentenceOrDamages;

    private Double damagesAmount; // optional numeric value for analytics

    private boolean landmarkCase;

    @Column(length = 300)
    private String keyTags;

    private int viewCount = 0;

    private int citationCount = 0;

    public LegalCase() {
    }

    public LegalCase(Long id, String caseNumber, String citation, String title, LegalDomain domain,
                     CourtLevel courtLevel, String courtName, String benchType, String presidingJudges,
                     String petitioner, String respondent, int filingYear, LocalDate judgmentDate,
                     int caseDurationMonths, String factsSynopsis, String legalIssues,
                     String statutesCited, String precedentsCited, String ratioDecidendi,
                     CaseOutcome outcome, String sentenceOrDamages, Double damagesAmount,
                     boolean landmarkCase, String keyTags, int viewCount, int citationCount) {
        this.id = id;
        this.caseNumber = caseNumber;
        this.citation = citation;
        this.title = title;
        this.domain = domain;
        this.courtLevel = courtLevel;
        this.courtName = courtName;
        this.benchType = benchType;
        this.presidingJudges = presidingJudges;
        this.petitioner = petitioner;
        this.respondent = respondent;
        this.filingYear = filingYear;
        this.judgmentDate = judgmentDate;
        this.caseDurationMonths = caseDurationMonths;
        this.factsSynopsis = factsSynopsis;
        this.legalIssues = legalIssues;
        this.statutesCited = statutesCited;
        this.precedentsCited = precedentsCited;
        this.ratioDecidendi = ratioDecidendi;
        this.outcome = outcome;
        this.sentenceOrDamages = sentenceOrDamages;
        this.damagesAmount = damagesAmount;
        this.landmarkCase = landmarkCase;
        this.keyTags = keyTags;
        this.viewCount = viewCount;
        this.citationCount = citationCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LegalDomain getDomain() {
        return domain;
    }

    public void setDomain(LegalDomain domain) {
        this.domain = domain;
    }

    public CourtLevel getCourtLevel() {
        return courtLevel;
    }

    public void setCourtLevel(CourtLevel courtLevel) {
        this.courtLevel = courtLevel;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public String getBenchType() {
        return benchType;
    }

    public void setBenchType(String benchType) {
        this.benchType = benchType;
    }

    public String getPresidingJudges() {
        return presidingJudges;
    }

    public void setPresidingJudges(String presidingJudges) {
        this.presidingJudges = presidingJudges;
    }

    public String getPetitioner() {
        return petitioner;
    }

    public void setPetitioner(String petitioner) {
        this.petitioner = petitioner;
    }

    public String getRespondent() {
        return respondent;
    }

    public void setRespondent(String respondent) {
        this.respondent = respondent;
    }

    public int getFilingYear() {
        return filingYear;
    }

    public void setFilingYear(int filingYear) {
        this.filingYear = filingYear;
    }

    public LocalDate getJudgmentDate() {
        return judgmentDate;
    }

    public void setJudgmentDate(LocalDate judgmentDate) {
        this.judgmentDate = judgmentDate;
    }

    public int getCaseDurationMonths() {
        return caseDurationMonths;
    }

    public void setCaseDurationMonths(int caseDurationMonths) {
        this.caseDurationMonths = caseDurationMonths;
    }

    public String getFactsSynopsis() {
        return factsSynopsis;
    }

    public void setFactsSynopsis(String factsSynopsis) {
        this.factsSynopsis = factsSynopsis;
    }

    public String getLegalIssues() {
        return legalIssues;
    }

    public void setLegalIssues(String legalIssues) {
        this.legalIssues = legalIssues;
    }

    public String getStatutesCited() {
        return statutesCited;
    }

    public void setStatutesCited(String statutesCited) {
        this.statutesCited = statutesCited;
    }

    public String getPrecedentsCited() {
        return precedentsCited;
    }

    public void setPrecedentsCited(String precedentsCited) {
        this.precedentsCited = precedentsCited;
    }

    public String getRatioDecidendi() {
        return ratioDecidendi;
    }

    public void setRatioDecidendi(String ratioDecidendi) {
        this.ratioDecidendi = ratioDecidendi;
    }

    public CaseOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(CaseOutcome outcome) {
        this.outcome = outcome;
    }

    public String getSentenceOrDamages() {
        return sentenceOrDamages;
    }

    public void setSentenceOrDamages(String sentenceOrDamages) {
        this.sentenceOrDamages = sentenceOrDamages;
    }

    public Double getDamagesAmount() {
        return damagesAmount;
    }

    public void setDamagesAmount(Double damagesAmount) {
        this.damagesAmount = damagesAmount;
    }

    public boolean isLandmarkCase() {
        return landmarkCase;
    }

    public void setLandmarkCase(boolean landmarkCase) {
        this.landmarkCase = landmarkCase;
    }

    public String getKeyTags() {
        return keyTags;
    }

    public void setKeyTags(String keyTags) {
        this.keyTags = keyTags;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getCitationCount() {
        return citationCount;
    }

    public void setCitationCount(int citationCount) {
        this.citationCount = citationCount;
    }
}
