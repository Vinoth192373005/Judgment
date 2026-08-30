package com.legalai.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Request payload containing case fact synopsis and legal parameters for AI recommendation.
 */
public class RecommendationRequest {

    @NotBlank(message = "Case fact summary is required for AI matching")
    private String factsSynopsis;

    private String legalIssues;

    private LegalDomain domain;

    private CourtLevel targetCourtLevel;

    private List<String> statutes; // e.g. ["Section 302 IPC", "Section 43A IT Act"]

    private String jurisdiction;

    private int topK = 5;

    // Optional customized weights (defaults to balanced legal AI model)
    private Double factWeight = 0.40;
    private Double statuteWeight = 0.25;
    private Double domainWeight = 0.15;
    private Double courtWeight = 0.10;
    private Double precedentWeight = 0.10;

    public RecommendationRequest() {
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

    public LegalDomain getDomain() {
        return domain;
    }

    public void setDomain(LegalDomain domain) {
        this.domain = domain;
    }

    public CourtLevel getTargetCourtLevel() {
        return targetCourtLevel;
    }

    public void setTargetCourtLevel(CourtLevel targetCourtLevel) {
        this.targetCourtLevel = targetCourtLevel;
    }

    public List<String> getStatutes() {
        return statutes;
    }

    public void setStatutes(List<String> statutes) {
        this.statutes = statutes;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public int getTopK() {
        return topK <= 0 ? 5 : topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public Double getFactWeight() {
        return factWeight == null ? 0.40 : factWeight;
    }

    public void setFactWeight(Double factWeight) {
        this.factWeight = factWeight;
    }

    public Double getStatuteWeight() {
        return statuteWeight == null ? 0.25 : statuteWeight;
    }

    public void setStatuteWeight(Double statuteWeight) {
        this.statuteWeight = statuteWeight;
    }

    public Double getDomainWeight() {
        return domainWeight == null ? 0.15 : domainWeight;
    }

    public void setDomainWeight(Double domainWeight) {
        this.domainWeight = domainWeight;
    }

    public Double getCourtWeight() {
        return courtWeight == null ? 0.10 : courtWeight;
    }

    public void setCourtWeight(Double courtWeight) {
        this.courtWeight = courtWeight;
    }

    public Double getPrecedentWeight() {
        return precedentWeight == null ? 0.10 : precedentWeight;
    }

    public void setPrecedentWeight(Double precedentWeight) {
        this.precedentWeight = precedentWeight;
    }
}
