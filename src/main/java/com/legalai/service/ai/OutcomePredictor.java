package com.legalai.service.ai;

import com.legalai.model.CaseOutcome;
import com.legalai.model.PredictionResult;
import com.legalai.model.RecommendationRequest;
import com.legalai.model.RecommendationResult.MatchedPrecedent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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

            // Collect influential metadata
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

        // Identify most likely outcome
        CaseOutcome predictedOutcome = outcomeWeights.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(CaseOutcome.PETITIONER_FAVOR);

        // Calculate win probabilities
        double petitionerWinProb = totalPrecedentWeight > 0 ? (proPlaintiffWeight / totalPrecedentWeight) * 100.0 : 50.0;
        double respondentWinProb = 100.0 - petitionerWinProb;

        // Confidence calculation based on consensus among top matches
        double topOutcomeWeight = outcomeWeights.getOrDefault(predictedOutcome, 0.0);
        double consensusRatio = totalPrecedentWeight > 0 ? (topOutcomeWeight / totalPrecedentWeight) : 0.6;
        double avgTopScore = topPrecedents.stream().mapToDouble(MatchedPrecedent::getOverallScore).average().orElse(70.0);
        double confidence = Math.min(96.5, Math.max(55.0, (avgTopScore * 0.6) + (consensusRatio * 40.0)));

        // Determine Risk Level & Rationale
        String riskLevel;
        String riskExplanation;
        if (petitionerWinProb >= 75.0) {
            riskLevel = "LOW RISK";
            riskExplanation = "Strong judicial consensus in favorable precedents. Core statutory requirements and factual elements align closely with established high-court doctrine.";
        } else if (petitionerWinProb >= 55.0) {
            riskLevel = "MODERATE RISK";
            riskExplanation = "Favorable precedent leaning exists, but opposing lines of authority or strict burden of proof require robust evidentiary substantiation.";
        } else if (petitionerWinProb >= 35.0) {
            riskLevel = "ELEVATED RISK";
            riskExplanation = "Contested legal posture. Precedents indicate heightened judicial scrutiny or defense doctrines (e.g. lack of privity, reasonable doubt, or procedural bar).";
        } else {
            riskLevel = "HIGH / CRITICAL RISK";
            riskExplanation = "Adverse precedent trajectory. Historical judgments indicate strong probability of petition dismissal or defendant-favored judgment unless novel constitutional arguments are framed.";
        }

        // Generate judicial reasoning synthesis
        StringBuilder reasoning = new StringBuilder();
        reasoning.append("Based on multi-vector analysis across ").append(topPrecedents.size())
                .append(" highly correlated precedents in ").append(request.getDomain() != null ? request.getDomain().getDisplayName() : "General Law")
                .append(", the predictive model indicates a ").append(String.format("%.1f%%", petitionerWinProb))
                .append(" probability of ruling favoring the Petitioner/Prosecution. ");

        if (!precedentTakeaways.isEmpty()) {
            reasoning.append("Primary judicial ratio indicates: ");
            reasoning.append(String.join("; ", precedentTakeaways)).append(". ");
        }

        reasoning.append("Key statutory focal points identified: ").append(String.join(", ", decisiveStatutes)).append(".");

        // Estimate remedy
        String remedyEstimate = deriveRemedyEstimate(predictedOutcome, topPrecedents);

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

    private String deriveRemedyEstimate(CaseOutcome outcome, List<MatchedPrecedent> precedents) {
        switch (outcome) {
            case PETITIONER_FAVOR:
                double avgDamages = precedents.stream()
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
                60.0,
                55.0,
                45.0,
                "MODERATE RISK",
                "Insufficient matching precedents in repository to form high-confidence prediction.",
                Collections.emptyList(),
                Collections.emptyList(),
                "Preliminary estimate based on general legal standards.",
                "Discretionary relief"
        );
    }
}
