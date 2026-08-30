package com.legalai.service;

import com.legalai.model.*;
import com.legalai.model.RecommendationResult.MatchedPrecedent;
import com.legalai.repository.LegalCaseRepository;
import com.legalai.service.ai.OutcomePredictor;
import com.legalai.service.ai.TFIDFVectorizer;
import com.legalai.service.ai.TextProcessor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AI Judgment Recommendation Service providing multi-factor precedent matching,
 * outcome prediction, and legal pleading arguments.
 */
@Service
public class RecommendationService {

    private final LegalCaseRepository caseRepository;
    private final TFIDFVectorizer vectorizer;
    private final TextProcessor textProcessor;
    private final OutcomePredictor outcomePredictor;

    // In-memory cache for corpus IDF and document vectors
    private final Map<Long, Map<String, Double>> caseVectorsCache = new ConcurrentHashMap<>();
    private Map<String, Double> idfCache = new HashMap<>();
    private volatile boolean isIndexed = false;

    public RecommendationService(LegalCaseRepository caseRepository,
                                 TFIDFVectorizer vectorizer,
                                 TextProcessor textProcessor,
                                 OutcomePredictor outcomePredictor) {
        this.caseRepository = caseRepository;
        this.vectorizer = vectorizer;
        this.textProcessor = textProcessor;
        this.outcomePredictor = outcomePredictor;
    }

    /**
     * Builds or refreshes TF-IDF vectors for all legal cases in repository.
     */
    public synchronized void reindexCorpus() {
        List<LegalCase> allCases = caseRepository.findAll();
        if (allCases.isEmpty()) {
            return;
        }

        List<String> caseCorpus = allCases.stream()
                .map(this::buildCaseSearchableText)
                .collect(Collectors.toList());

        this.idfCache = vectorizer.computeIDF(caseCorpus);
        this.caseVectorsCache.clear();

        for (LegalCase lc : allCases) {
            String combinedText = buildCaseSearchableText(lc);
            Map<String, Double> vector = vectorizer.vectorize(combinedText, idfCache);
            this.caseVectorsCache.put(lc.getId(), vector);
        }

        this.isIndexed = true;
    }

    /**
     * Analyzes input case facts and generates top precedent recommendations + outcome predictions.
     */
    public RecommendationResult recommend(RecommendationRequest request) {
        if (!isIndexed || caseVectorsCache.isEmpty()) {
            reindexCorpus();
        }

        List<LegalCase> allCases = caseRepository.findAll();
        if (allCases.isEmpty()) {
            return new RecommendationResult(Collections.emptyList(), null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "Repository is empty.");
        }

        // Vectorize query
        String queryText = (request.getFactsSynopsis() != null ? request.getFactsSynopsis() : "") + " "
                + (request.getLegalIssues() != null ? request.getLegalIssues() : "");
        Map<String, Double> queryVector = vectorizer.vectorize(queryText, idfCache);

        List<MatchedPrecedent> matches = new ArrayList<>();

        for (LegalCase lc : allCases) {
            Map<String, Double> caseVector = caseVectorsCache.get(lc.getId());
            if (caseVector == null) {
                String cText = buildCaseSearchableText(lc);
                caseVector = vectorizer.vectorize(cText, idfCache);
                caseVectorsCache.put(lc.getId(), caseVector);
            }

            // 1. Fact & Legal Issue Similarity (0 - 100)
            double factSim = vectorizer.cosineSimilarity(queryVector, caseVector) * 100.0;

            // 2. Statute Overlap (0 - 100)
            double statuteSim = vectorizer.computeStatuteOverlap(request.getStatutes(), lc.getStatutesCited()) * 100.0;

            // 3. Domain Alignment Score (0 - 100)
            double domainScore = 20.0;
            if (request.getDomain() != null) {
                if (request.getDomain() == lc.getDomain()) {
                    domainScore = 100.0;
                } else if (isRelatedDomain(request.getDomain(), lc.getDomain())) {
                    domainScore = 60.0;
                }
            } else {
                domainScore = 70.0; // neutral if no domain specified
            }

            // 4. Court Precedent Weight (0 - 100)
            double courtScore = (lc.getCourtLevel() != null ? lc.getCourtLevel().getPrecedentWeight() : 0.6) * 100.0;
            if (lc.isLandmarkCase()) {
                courtScore = Math.min(100.0, courtScore + 15.0);
            }

            // Calculate Weighted Overall Score
            double wFact = request.getFactWeight();
            double wStat = request.getStatuteWeight();
            double wDom = request.getDomainWeight();
            double wCourt = request.getCourtWeight();
            double wPrec = request.getPrecedentWeight();
            double sumWeights = wFact + wStat + wDom + wCourt + wPrec;

            double landmarkBoost = lc.isLandmarkCase() ? 100.0 : (Math.min(100.0, lc.getCitationCount() * 10.0));

            double overallScore = (
                    (factSim * wFact) +
                    (statuteSim * wStat) +
                    (domainScore * wDom) +
                    (courtScore * wCourt) +
                    (landmarkBoost * wPrec)
            ) / sumWeights;

            // Determine if binding on target court
            boolean isBinding = isBindingPrecedent(lc.getCourtLevel(), request.getTargetCourtLevel());

            String rationale = generateMatchRationale(lc, factSim, statuteSim, request);
            String takeaway = extractTakeaway(lc);

            matches.add(new MatchedPrecedent(
                    lc,
                    Math.round(overallScore * 10.0) / 10.0,
                    Math.round(factSim * 10.0) / 10.0,
                    Math.round(statuteSim * 10.0) / 10.0,
                    Math.round(domainScore * 10.0) / 10.0,
                    Math.round(courtScore * 10.0) / 10.0,
                    rationale,
                    takeaway,
                    isBinding
            ));
        }

        // Sort descending by overall score
        matches.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));

        // Take top K matches
        int topK = Math.min(request.getTopK(), matches.size());
        List<MatchedPrecedent> topPrecedents = matches.subList(0, topK);

        // Predict outcome based on top precedents
        PredictionResult prediction = outcomePredictor.predict(request, topPrecedents);

        // Generate legal arguments, statutes, and risk factors
        List<String> suggestedArguments = generateSuggestedArguments(request, topPrecedents);
        List<String> statutesToCite = generateStatutesToCite(request, topPrecedents);
        List<String> riskFactors = generateRiskFactors(prediction, topPrecedents);

        String summary = String.format("AI evaluated %d repository cases. Identified %d high-affinity precedents with average match confidence of %.1f%%.",
                allCases.size(), topPrecedents.size(),
                topPrecedents.stream().mapToDouble(MatchedPrecedent::getOverallScore).average().orElse(0.0));

        return new RecommendationResult(
                topPrecedents,
                prediction,
                suggestedArguments,
                statutesToCite,
                riskFactors,
                summary
        );
    }

    private String buildCaseSearchableText(LegalCase lc) {
        StringBuilder sb = new StringBuilder();
        if (lc.getTitle() != null) sb.append(lc.getTitle()).append(" ");
        if (lc.getFactsSynopsis() != null) sb.append(lc.getFactsSynopsis()).append(" ");
        if (lc.getLegalIssues() != null) sb.append(lc.getLegalIssues()).append(" ");
        if (lc.getRatioDecidendi() != null) sb.append(lc.getRatioDecidendi()).append(" ");
        if (lc.getStatutesCited() != null) sb.append(lc.getStatutesCited()).append(" ");
        if (lc.getKeyTags() != null) sb.append(lc.getKeyTags()).append(" ");
        return sb.toString();
    }

    private boolean isRelatedDomain(LegalDomain d1, LegalDomain d2) {
        if (d1 == null || d2 == null) return false;
        if ((d1 == LegalDomain.CYBER_DEFAMATION && d2 == LegalDomain.INTELLECTUAL_PROPERTY) ||
            (d1 == LegalDomain.INTELLECTUAL_PROPERTY && d2 == LegalDomain.CORPORATE_COMMERCIAL) ||
            (d1 == LegalDomain.CONSTITUTIONAL && d2 == LegalDomain.CRIMINAL) ||
            (d1 == LegalDomain.CIVIL_TORT && d2 == LegalDomain.ENVIRONMENTAL)) {
            return true;
        }
        return false;
    }

    private boolean isBindingPrecedent(CourtLevel precedentLevel, CourtLevel targetLevel) {
        if (precedentLevel == null) return false;
        if (precedentLevel == CourtLevel.SUPREME_COURT) return true; // Supreme Court is binding nationwide
        if (targetLevel == null) return false;
        return precedentLevel.ordinal() <= targetLevel.ordinal();
    }

    private String generateMatchRationale(LegalCase lc, double factSim, double statSim, RecommendationRequest request) {
        if (statSim > 40.0 && factSim > 30.0) {
            return "Strong direct alignment in statutory provisions and underlying transactional/factual matrices.";
        } else if (lc.isLandmarkCase()) {
            return "Landmark authority establishing binding judicial principles and legal interpretation.";
        } else if (factSim > 35.0) {
            return "Substantial factual analogy regarding core conduct, liability triggers, and legal issues.";
        } else if (statSim > 30.0) {
            return "Shared statutory focus providing persuasive interpretive guidelines.";
        } else {
            return "Persuasive comparative precedent within corresponding jurisdictional jurisprudence.";
        }
    }

    private String extractTakeaway(LegalCase lc) {
        if (lc.getRatioDecidendi() != null && !lc.getRatioDecidendi().isBlank()) {
            return lc.getRatioDecidendi();
        }
        return lc.getTitle() + " ruled outcome: " + lc.getOutcome().getDisplayName();
    }

    private List<String> generateSuggestedArguments(RecommendationRequest request, List<MatchedPrecedent> precedents) {
        List<String> args = new ArrayList<>();
        if (!precedents.isEmpty()) {
            MatchedPrecedent top = precedents.get(0);
            args.add(String.format("Assert established precedent under %s (%s), which affirms that the burden of proof is satisfied when essential elements are corroborated.",
                    top.getLegalCase().getTitle(), top.getLegalCase().getCitation()));
        }

        if (request.getStatutes() != null && !request.getStatutes().isEmpty()) {
            args.add(String.format("Plead statutory violation under %s, emphasizing continuous harm and strict liability standards.",
                    String.join(" and ", request.getStatutes())));
        } else {
            args.add("Plead breach of fiduciary duty and violation of statutory due diligence guidelines.");
        }

        args.add("Highlight proportional remedy and injunctive relief to prevent irreparable damages pending final adjudication.");
        args.add("Distinguish adverse trial court interpretations by citing authoritative Apex Court ratio decidendi.");
        return args;
    }

    private List<String> generateStatutesToCite(RecommendationRequest request, List<MatchedPrecedent> precedents) {
        Set<String> statutes = new LinkedHashSet<>();
        if (request.getStatutes() != null) {
            statutes.addAll(request.getStatutes());
        }
        for (MatchedPrecedent mp : precedents) {
            if (mp.getLegalCase().getStatutesCited() != null) {
                for (String s : mp.getLegalCase().getStatutesCited().split("[,;]")) {
                    String clean = s.trim();
                    if (!clean.isBlank() && statutes.size() < 6) {
                        statutes.add(clean);
                    }
                }
            }
        }
        return new ArrayList<>(statutes);
    }

    private List<String> generateRiskFactors(PredictionResult prediction, List<MatchedPrecedent> precedents) {
        List<String> risks = new ArrayList<>();
        if (prediction.getConfidencePercentage() < 70.0) {
            risks.add("Moderate precedent divergence: differing benches have rendered contextual interpretations.");
        }
        risks.add("Statutory limitation and procedural compliance must be rigorously documented.");
        risks.add("Opposing counsel may argue factual distinction based on standard of care or contractual indemnity clauses.");
        if (prediction.getPetitionerWinProbability() < 50.0) {
            risks.add("High probability of preliminary demurrer or motion to dismiss based on jurisdictional thresholds.");
        }
        return risks;
    }
}
