package com.legalai.model;

/**
 * Filter and query criteria for searching the legal case repository.
 */
public class CaseSearchCriteria {
    private String query;
    private LegalDomain domain;
    private CourtLevel courtLevel;
    private CaseOutcome outcome;
    private Boolean landmarkOnly;
    private Integer startYear;
    private Integer endYear;
    private String statute;
    private String judge;

    public CaseSearchCriteria() {
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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

    public CaseOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(CaseOutcome outcome) {
        this.outcome = outcome;
    }

    public Boolean getLandmarkOnly() {
        return landmarkOnly;
    }

    public void setLandmarkOnly(Boolean landmarkOnly) {
        this.landmarkOnly = landmarkOnly;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public void setEndYear(Integer endYear) {
        this.endYear = endYear;
    }

    public String getStatute() {
        return statute;
    }

    public void setStatute(String statute) {
        this.statute = statute;
    }

    public String getJudge() {
        return judge;
    }

    public void setJudge(String judge) {
        this.judge = judge;
    }
}
