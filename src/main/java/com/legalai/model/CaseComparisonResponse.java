package com.legalai.model;

import java.util.List;

/**
 * Payload for side-by-side comparative analysis of selected legal cases.
 */
public class CaseComparisonResponse {

    private List<LegalCase> cases;
    private List<String> sharedStatutes;
    private List<String> sharedPrecedents;
    private List<String> keyDivergences;
    private String analyticalComparison;

    public CaseComparisonResponse() {
    }

    public CaseComparisonResponse(List<LegalCase> cases, List<String> sharedStatutes,
                                  List<String> sharedPrecedents, List<String> keyDivergences,
                                  String analyticalComparison) {
        this.cases = cases;
        this.sharedStatutes = sharedStatutes;
        this.sharedPrecedents = sharedPrecedents;
        this.keyDivergences = keyDivergences;
        this.analyticalComparison = analyticalComparison;
    }

    public List<LegalCase> getCases() {
        return cases;
    }

    public void setCases(List<LegalCase> cases) {
        this.cases = cases;
    }

    public List<String> getSharedStatutes() {
        return sharedStatutes;
    }

    public void setSharedStatutes(List<String> sharedStatutes) {
        this.sharedStatutes = sharedStatutes;
    }

    public List<String> getSharedPrecedents() {
        return sharedPrecedents;
    }

    public void setSharedPrecedents(List<String> sharedPrecedents) {
        this.sharedPrecedents = sharedPrecedents;
    }

    public List<String> getKeyDivergences() {
        return keyDivergences;
    }

    public void setKeyDivergences(List<String> keyDivergences) {
        this.keyDivergences = keyDivergences;
    }

    public String getAnalyticalComparison() {
        return analyticalComparison;
    }

    public void setAnalyticalComparison(String analyticalComparison) {
        this.analyticalComparison = analyticalComparison;
    }
}
