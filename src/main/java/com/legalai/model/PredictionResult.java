package com.legalai.model;

import java.util.List;

/**
 * Outcome prediction model encapsulating win/loss probabilities, confidence index, and risk assessment.
 */
public class PredictionResult {

    private CaseOutcome predictedOutcome;
    private double confidencePercentage;
    private double petitionerWinProbability;
    private double respondentWinProbability;
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String riskExplanation;
    private List<String> decisiveStatutes;
    private List<String> influentialPrecedents;
    private String judicialReasoning;
    private String estimatedRemedyOrSentence;

    public PredictionResult() {
    }

    public PredictionResult(CaseOutcome predictedOutcome, double confidencePercentage,
                            double petitionerWinProbability, double respondentWinProbability,
                            String riskLevel, String riskExplanation, List<String> decisiveStatutes,
                            List<String> influentialPrecedents, String judicialReasoning,
                            String estimatedRemedyOrSentence) {
        this.predictedOutcome = predictedOutcome;
        this.confidencePercentage = confidencePercentage;
        this.petitionerWinProbability = petitionerWinProbability;
        this.respondentWinProbability = respondentWinProbability;
        this.riskLevel = riskLevel;
        this.riskExplanation = riskExplanation;
        this.decisiveStatutes = decisiveStatutes;
        this.influentialPrecedents = influentialPrecedents;
        this.judicialReasoning = judicialReasoning;
        this.estimatedRemedyOrSentence = estimatedRemedyOrSentence;
    }

    public CaseOutcome getPredictedOutcome() {
        return predictedOutcome;
    }

    public void setPredictedOutcome(CaseOutcome predictedOutcome) {
        this.predictedOutcome = predictedOutcome;
    }

    public double getConfidencePercentage() {
        return confidencePercentage;
    }

    public void setConfidencePercentage(double confidencePercentage) {
        this.confidencePercentage = confidencePercentage;
    }

    public double getPetitionerWinProbability() {
        return petitionerWinProbability;
    }

    public void setPetitionerWinProbability(double petitionerWinProbability) {
        this.petitionerWinProbability = petitionerWinProbability;
    }

    public double getRespondentWinProbability() {
        return respondentWinProbability;
    }

    public void setRespondentWinProbability(double respondentWinProbability) {
        this.respondentWinProbability = respondentWinProbability;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskExplanation() {
        return riskExplanation;
    }

    public void setRiskExplanation(String riskExplanation) {
        this.riskExplanation = riskExplanation;
    }

    public List<String> getDecisiveStatutes() {
        return decisiveStatutes;
    }

    public void setDecisiveStatutes(List<String> decisiveStatutes) {
        this.decisiveStatutes = decisiveStatutes;
    }

    public List<String> getInfluentialPrecedents() {
        return influentialPrecedents;
    }

    public void setInfluentialPrecedents(List<String> influentialPrecedents) {
        this.influentialPrecedents = influentialPrecedents;
    }

    public String getJudicialReasoning() {
        return judicialReasoning;
    }

    public void setJudicialReasoning(String judicialReasoning) {
        this.judicialReasoning = judicialReasoning;
    }

    public String getEstimatedRemedyOrSentence() {
        return estimatedRemedyOrSentence;
    }

    public void setEstimatedRemedyOrSentence(String estimatedRemedyOrSentence) {
        this.estimatedRemedyOrSentence = estimatedRemedyOrSentence;
    }
}
