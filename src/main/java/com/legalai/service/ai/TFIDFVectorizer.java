package com.legalai.service.ai;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * TF-IDF Vectorizer and Cosine Similarity computational engine for legal text matching.
 */
@Component
public class TFIDFVectorizer {

    private final TextProcessor textProcessor;

    public TFIDFVectorizer(TextProcessor textProcessor) {
        this.textProcessor = textProcessor;
    }

    /**
     * Computes IDF values for all terms across a corpus of documents.
     */
    public Map<String, Double> computeIDF(List<String> documents) {
        Map<String, Integer> docFrequencies = new HashMap<>();
        int totalDocs = documents.size();

        for (String doc : documents) {
            Set<String> uniqueTerms = new HashSet<>(textProcessor.tokenize(doc));
            for (String term : uniqueTerms) {
                docFrequencies.put(term, docFrequencies.getOrDefault(term, 0) + 1);
            }
        }

        Map<String, Double> idfMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : docFrequencies.entrySet()) {
            int df = entry.getValue();
            // Smooth IDF formula: ln(1 + (N - df + 0.5) / (df + 0.5)) + 1
            double idf = Math.log(1.0 + ((double) (totalDocs - df + 0.5) / (df + 0.5))) + 1.0;
            idfMap.put(entry.getKey(), idf);
        }

        return idfMap;
    }

    /**
     * Converts a document text into a normalized TF-IDF vector (term -> weight).
     */
    public Map<String, Double> vectorize(String text, Map<String, Double> idfMap) {
        Map<String, Integer> tfMap = textProcessor.getTermFrequencies(text);
        if (tfMap.isEmpty()) {
            return Collections.emptyMap();
        }

        int maxTf = tfMap.values().stream().max(Integer::compareTo).orElse(1);
        Map<String, Double> tfidfVector = new HashMap<>();
        double sumSquares = 0.0;

        for (Map.Entry<String, Integer> entry : tfMap.entrySet()) {
            String term = entry.getKey();
            int count = entry.getValue();

            // Augmented Term Frequency to prevent bias towards longer texts
            double tf = 0.5 + 0.5 * ((double) count / maxTf);
            double idf = idfMap.getOrDefault(term, 1.0); // Default fallback IDF
            double weight = tf * idf;

            tfidfVector.put(term, weight);
            sumSquares += weight * weight;
        }

        // L2 Unit Normalization
        double magnitude = Math.sqrt(sumSquares);
        if (magnitude > 0) {
            for (Map.Entry<String, Double> entry : tfidfVector.entrySet()) {
                entry.setValue(entry.getValue() / magnitude);
            }
        }

        return tfidfVector;
    }

    /**
     * Calculates the Cosine Similarity between two TF-IDF vectors (0.0 to 1.0).
     */
    public double cosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        if (vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        // Iterate through smaller map for efficiency
        Map<String, Double> smaller = vector1.size() < vector2.size() ? vector1 : vector2;
        Map<String, Double> larger = vector1.size() < vector2.size() ? vector2 : vector1;

        for (Map.Entry<String, Double> entry : smaller.entrySet()) {
            Double valInLarger = larger.get(entry.getKey());
            if (valInLarger != null) {
                dotProduct += entry.getValue() * valInLarger;
            }
        }

        // Clip to [0.0, 1.0] range
        return Math.max(0.0, Math.min(1.0, dotProduct));
    }

    /**
     * Calculates Jaccard similarity and token overlap between cited statutes.
     */
    public double computeStatuteOverlap(List<String> queryStatutes, String caseStatutesString) {
        if (queryStatutes == null || queryStatutes.isEmpty() || caseStatutesString == null || caseStatutesString.isBlank()) {
            return 0.0;
        }

        Set<String> queryTokens = new HashSet<>();
        for (String q : queryStatutes) {
            queryTokens.addAll(textProcessor.tokenize(q));
        }

        Set<String> caseTokens = new HashSet<>(textProcessor.tokenize(caseStatutesString));

        if (queryTokens.isEmpty() || caseTokens.isEmpty()) {
            return 0.0;
        }

        // Compute intersection and union
        Set<String> intersection = new HashSet<>(queryTokens);
        intersection.retainAll(caseTokens);

        Set<String> union = new HashSet<>(queryTokens);
        union.addAll(caseTokens);

        double jaccard = (double) intersection.size() / union.size();

        // Exact substring bonus
        double substringBonus = 0.0;
        String lowerCaseStatutes = caseStatutesString.toLowerCase(Locale.ROOT);
        for (String q : queryStatutes) {
            if (lowerCaseStatutes.contains(q.trim().toLowerCase(Locale.ROOT))) {
                substringBonus += 0.25;
            }
        }

        return Math.min(1.0, (jaccard * 0.75) + substringBonus);
    }
}
