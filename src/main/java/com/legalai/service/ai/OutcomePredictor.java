package com.legalai.service.ai;

import com.legalai.model.CaseOutcome;
import com.legalai.model.PredictionResult;
import com.legalai.model.RecommendationRequest;
import com.legalai.model.RecommendationResult.MatchedPrecedent;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * AI Outcome Predictor using weighted probabilistic classification,
 * precedent ratio synthesis, and statutory risk analysis.
 */
@Component
public class OutcomePredictor {

    /**
     * Synthesizes outcome probabilities, risk level, decisive factors, and judicial reasoning.
     */
    public PredictionResult predict(RecommendationRequest request, List<MatchedPrecedent> topPrecedents) {
        if (topPrecedents == null || topPrecedents.isEmpty()) {
            return fallbackPrediction();
        }

        // Weighted outcome accumulator
        Map<CaseOutcome, Double> outcomeWeights = new EnumMap<>(CaseOutcome.class);
        double totalPrecedentWeight = 0.0;
        double proPlaintiffWeight = 0.0;
        double proDefendantWeight = 0.0;

        List<String> decisiveStatutes = new ArrayList<>();
        List<String> influentialPrecedents = new ArrayList<>();
        List<String> precedentTakeaways = new ArrayList<>();

        for (MatchedPrecedent mp : topPrecedents) {
            double score = mp.getOverallScore() / 100.0; // scale 0-1
            if (mp.isBindingPrecedent()) {
                score *= 1.25; // Boost binding precedents
            }

            CaseOutcome outcome = mp.getLegalCase().getOutcome();
            outcomeWeights.put(outcome, outcomeWeights.getOrDefault(outcome, 0.0) + score);
            totalPrecedentWeight += score;

            if (outcome.isProPlaintiffOrPetitioner()) {
                proPlaintiffWeight += score;
            } else if (outcome.isProDefendantOrRespondent()) {
                proDefendantWeight += score;
            } else {
                // Neutral or remanded
                proPlaintiffWeight += score * 0.5;
                proDefendantWeight += score * 0.5;
            }

            // Only collect metadata from precedents with genuine relevance
            if (mp.getFactSimilarity() >= 12.0 || mp.getStatuteSimilarity() >= 20.0 || mp.getOverallScore() >= 45.0) {
                if (influentialPrecedents.size() < 4) {
                    influentialPrecedents.add(mp.getLegalCase().getTitle() + " (" + mp.getLegalCase().getCitation() + ")");
                }
                if (mp.getLegalCase().getStatutesCited() != null) {
                    for (String stat : mp.getLegalCase().getStatutesCited().split("[,;]")) {
                        String cleanStat = stat.trim();
                        if (!cleanStat.isBlank() && !decisiveStatutes.contains(cleanStat) && decisiveStatutes.size() < 5) {
                            decisiveStatutes.add(cleanStat);
                        }
                    }
                }
                if (mp.getKeyTakeaway() != null && !mp.getKeyTakeaway().isBlank() && precedentTakeaways.size() < 3) {
                    precedentTakeaways.add(mp.getKeyTakeaway());
                }
            }
        }

        // Identify most likely outcome
        CaseOutcome predictedOutcome = outcomeWeights.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(CaseOutcome.PETITIONER_FAVOR);

        // Calculate win probabilities
        double petitionerWinProb = totalPrecedentWeight > 0 ? (proPlaintiffWeight / totalPrecedentWeight) * 100.0 : 50.0;
        double respondentWinProb = 100.0 - petitionerWinProb;

        // Confidence calculation based on factual correlation and consensus
        double topOutcomeWeight = outcomeWeights.getOrDefault(predictedOutcome, 0.0);
        double consensusRatio = totalPrecedentWeight > 0 ? (topOutcomeWeight / totalPrecedentWeight) : 0.6;
        double avgTopScore = topPrecedents.stream().mapToDouble(MatchedPrecedent::getOverallScore).average().orElse(50.0);
        double topScore = topPrecedents.isEmpty() ? 50.0 : topPrecedents.get(0).getOverallScore();
        double maxFactCosine = topPrecedents.stream().mapToDouble(MatchedPrecedent::getFactSimilarity).max().orElse(0.0);

        double baseConfidence = (topScore * 0.45) + (avgTopScore * 0.25) + (consensusRatio * 30.0);
        double confidence = Math.min(96.0, Math.max(45.0, baseConfidence));

        // Determine Risk Level & Rationale
        String riskLevel;
        String riskExplanation;
        if (maxFactCosine < 15.0) {
            riskLevel = "LOW PRECEDENT DENSITY";
            riskExplanation = "Limited factual correlation with local repository. Outcome synthesized based on legal domain averages and statutory burdens of proof.";
        } else if (petitionerWinProb >= 75.0) {
            riskLevel = "LOW RISK";
            riskExplanation = "Strong judicial consensus in favorable precedents. Core statutory requirements and factual elements align closely with established doctrine.";
        } else if (petitionerWinProb >= 55.0) {
            riskLevel = "MODERATE RISK";
            riskExplanation = "Favorable precedent trajectory exists, but opposing lines of authority require robust evidentiary substantiation.";
        } else if (petitionerWinProb >= 35.0) {
            riskLevel = "ELEVATED RISK";
            riskExplanation = "Contested legal posture. Precedents indicate heightened judicial scrutiny or defense doctrines.";
        } else {
            riskLevel = "HIGH / CRITICAL RISK";
            riskExplanation = "Adverse precedent trajectory. Historical judgments indicate strong probability of petition dismissal or defendant-favored ruling.";
        }

        // Generate contextual judicial reasoning synthesis
        StringBuilder reasoning = new StringBuilder();
        String domainName = request.getDomain() != null ? request.getDomain().getDisplayName() : "General Law";

        if (maxFactCosine < 15.0) {
            reasoning.append("Preliminary assessment for ")
                    .append(domainName)
                    .append(" claim indicates a projected ")
                    .append(String.format("%.1f%%", petitionerWinProb))
                    .append(" probability favoring the claimant based on general statutory standards. ");
            if (!decisiveStatutes.isEmpty()) {
                reasoning.append("Applicable statutory provisions: ").append(String.join(", ", decisiveStatutes)).append(". ");
            }
            reasoning.append("For higher precision, provide additional factual details (e.g. policy terms, collision timeline, surveyor report) or query CourtListener API for specific live precedents.");
        } else {
            reasoning.append("Based on multi-vector analysis across ")
                    .append(topPrecedents.size())
                    .append(" correlated precedents in ")
                    .append(domainName)
                    .append(", the predictive model indicates a ")
                    .append(String.format("%.1f%%", petitionerWinProb))
                    .append(" probability of ruling favoring the Petitioner/Prosecution. ");

            if (!precedentTakeaways.isEmpty()) {
                reasoning.append("Primary judicial ratios indicate: ");
                reasoning.append(String.join("; ", precedentTakeaways)).append(". ");
            }

            if (!decisiveStatutes.isEmpty()) {
                reasoning.append("Key statutory focal points: ").append(String.join(", ", decisiveStatutes)).append(".");
            }
        }

        // Estimate remedy
        String remedyEstimate = deriveRemedyEstimate(predictedOutcome, topPrecedents, maxFactCosine);

        return new PredictionResult(
                predictedOutcome,
                Math.round(confidence * 10.0) / 10.0,
                Math.round(petitionerWinProb * 10.0) / 10.0,
                Math.round(respondentWinProb * 10.0) / 10.0,
                riskLevel,
                riskExplanation,
                decisiveStatutes,
                influentialPrecedents,
                reasoning.toString(),
                remedyEstimate
        );
    }

    private String deriveRemedyEstimate(CaseOutcome outcome, List<MatchedPrecedent> precedents, double maxFactCosine) {
        if (maxFactCosine < 15.0) {
            return "Standard statutory remedy & damages assessed according to proven economic loss";
        }
        switch (outcome) {
            case PETITIONER_FAVOR:
                double avgDamages = precedents.stream()
                        .filter(mp -> mp.getFactSimilarity() >= 15.0)
                        .map(mp -> mp.getLegalCase().getDamagesAmount())
                        .filter(Objects::nonNull)
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
                if (avgDamages > 0) {
                    return String.format("Injunctive relief + Estimated damages range: $%,.0f - $%,.0f", avgDamages * 0.8, avgDamages * 1.3);
                }
                return "Permanent Injunction & Declaratory Relief with Costs";
            case CONVICTED:
                return "Conviction under substantive penal provisions; Statutory sentencing guidelines apply";
            case ACQUITTED:
                return "Full Acquittal; Release of bonds and dismissal of indictment";
            case DISMISSED:
                return "Petition dismissed with no relief granted; Costs may be imposed";
            case SETTLED:
                return "Court-supervised structured settlement and consent decree";
            default:
                return "Standard statutory remedy according to trial court discretion";
        }
    }

    private PredictionResult fallbackPrediction() {
        return new PredictionResult(
                CaseOutcome.PETITIONER_FAVOR,
                50.0,
                55.0,
                45.0,
                "LOW PRECEDENT DENSITY",
                "Insufficient matching precedents in repository to form high-confidence prediction.",
                Collections.emptyList(),
                Collections.emptyList(),
                "Preliminary estimate based on general legal standards. Please provide more detailed factual background or query live CourtListener API.",
                "Discretionary relief"
        );
    }
}
