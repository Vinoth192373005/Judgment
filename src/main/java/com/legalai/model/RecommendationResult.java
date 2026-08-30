package com.legalai.model;

import java.util.List;

/**
 * Detailed outcome and precedent recommendation result payload.
 */
public class RecommendationResult {

    private List<MatchedPrecedent> topPrecedents;
    private PredictionResult outcomePrediction;
    private List<String> suggestedLegalArguments;
    private List<String> keyStatutesToCite;
    private List<String> riskFactors;
    private String analyticalSummary;

    public RecommendationResult() {
    }

    public RecommendationResult(List<MatchedPrecedent> topPrecedents, PredictionResult outcomePrediction,
                                  List<String> suggestedLegalArguments, List<String> keyStatutesToCite,
                                  List<String> riskFactors, String analyticalSummary) {
        this.topPrecedents = topPrecedents;
        this.outcomePrediction = outcomePrediction;
        this.suggestedLegalArguments = suggestedLegalArguments;
        this.keyStatutesToCite = keyStatutesToCite;
        this.riskFactors = riskFactors;
        this.analyticalSummary = analyticalSummary;
    }

    public List<MatchedPrecedent> getTopPrecedents() {
        return topPrecedents;
    }

    public void setTopPrecedents(List<MatchedPrecedent> topPrecedents) {
        this.topPrecedents = topPrecedents;
    }

    public PredictionResult getOutcomePrediction() {
        return outcomePrediction;
    }

    public void setOutcomePrediction(PredictionResult outcomePrediction) {
        this.outcomePrediction = outcomePrediction;
    }

    public List<String> getSuggestedLegalArguments() {
        return suggestedLegalArguments;
    }

    public void setSuggestedLegalArguments(List<String> suggestedLegalArguments) {
        this.suggestedLegalArguments = suggestedLegalArguments;
    }

    public List<String> getKeyStatutesToCite() {
        return keyStatutesToCite;
    }

    public void setKeyStatutesToCite(List<String> keyStatutesToCite) {
        this.keyStatutesToCite = keyStatutesToCite;
    }

    public List<String> getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(List<String> riskFactors) {
        this.riskFactors = riskFactors;
    }

    public String getAnalyticalSummary() {
        return analyticalSummary;
    }

    public void setAnalyticalSummary(String analyticalSummary) {
        this.analyticalSummary = analyticalSummary;
    }

    /**
     * Nested class representing a single matched precedent case with detailed similarity score breakdown.
     */
    public static class MatchedPrecedent {
        private LegalCase legalCase;
        private double overallScore; // 0-100%
        private double factSimilarity; // 0-100%
        private double statuteSimilarity; // 0-100%
        private double domainScore; // 0-100%
        private double courtPrecedentScore; // 0-100%
        private String matchRationale;
        private String keyTakeaway;
        private boolean bindingPrecedent;

        public MatchedPrecedent() {
        }

        public MatchedPrecedent(LegalCase legalCase, double overallScore, double factSimilarity,
                                double statuteSimilarity, double domainScore, double courtPrecedentScore,
                                String matchRationale, String keyTakeaway, boolean bindingPrecedent) {
            this.legalCase = legalCase;
            this.overallScore = overallScore;
            this.factSimilarity = factSimilarity;
            this.statuteSimilarity = statuteSimilarity;
            this.domainScore = domainScore;
            this.courtPrecedentScore = courtPrecedentScore;
            this.matchRationale = matchRationale;
            this.keyTakeaway = keyTakeaway;
            this.bindingPrecedent = bindingPrecedent;
        }

        public LegalCase getLegalCase() {
            return legalCase;
        }

        public void setLegalCase(LegalCase legalCase) {
            this.legalCase = legalCase;
        }

        public double getOverallScore() {
            return overallScore;
        }

        public void setOverallScore(double overallScore) {
            this.overallScore = overallScore;
        }

        public double getFactSimilarity() {
            return factSimilarity;
        }

        public void setFactSimilarity(double factSimilarity) {
            this.factSimilarity = factSimilarity;
        }

        public double getStatuteSimilarity() {
            return statuteSimilarity;
        }

        public void setStatuteSimilarity(double statuteSimilarity) {
            this.statuteSimilarity = statuteSimilarity;
        }

        public double getDomainScore() {
            return domainScore;
        }

        public void setDomainScore(double domainScore) {
            this.domainScore = domainScore;
        }

        public double getCourtPrecedentScore() {
            return courtPrecedentScore;
        }

        public void setCourtPrecedentScore(double courtPrecedentScore) {
            this.courtPrecedentScore = courtPrecedentScore;
        }

        public String getMatchRationale() {
            return matchRationale;
        }

        public void setMatchRationale(String matchRationale) {
            this.matchRationale = matchRationale;
        }

        public String getKeyTakeaway() {
            return keyTakeaway;
        }

        public void setKeyTakeaway(String keyTakeaway) {
            this.keyTakeaway = keyTakeaway;
        }

        public boolean isBindingPrecedent() {
            return bindingPrecedent;
        }

        public void setBindingPrecedent(boolean bindingPrecedent) {
            this.bindingPrecedent = bindingPrecedent;
        }
    }
}
