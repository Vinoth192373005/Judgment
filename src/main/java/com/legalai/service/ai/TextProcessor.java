package com.legalai.service.ai;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Natural Language Processing component for legal text tokenization,
 * legal stop-word filtering, term normalization, and keyphrase extraction.
 */
@Component
public class TextProcessor {

    private static final Pattern WORD_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can",
            "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down",
            "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", "haven't",
            "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself",
            "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it",
            "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not",
            "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over",
            "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such",
            "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's",
            "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too",
            "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were",
            "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's",
            "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're",
            "you've", "your", "yours", "yourself", "yourselves",
            // General filler words
            "court", "case", "matter", "appellant", "respondent", "plaintiff", "defendant", "honorable",
            "learned", "versus", "vs", "v", "said", "also", "wherein", "thereof", "herein", "thereby"
    ));

    /**
     * Tokenizes raw text into clean, lowercased, stemmed tokens with stop-words removed.
     */
    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String[] rawTokens = WORD_PATTERN.split(text.toLowerCase(Locale.ROOT));
        List<String> cleanedTokens = new ArrayList<>();

        for (String raw : rawTokens) {
            String trimmed = raw.trim();
            if (trimmed.length() > 2 && !STOP_WORDS.contains(trimmed)) {
                cleanedTokens.add(normalizeStem(trimmed));
            }
        }

        return cleanedTokens;
    }

    /**
     * Extracts token frequency map from text.
     */
    public Map<String, Integer> getTermFrequencies(String text) {
        List<String> tokens = tokenize(text);
        Map<String, Integer> freqMap = new HashMap<>();
        for (String token : tokens) {
            freqMap.put(token, freqMap.getOrDefault(token, 0) + 1);
        }
        return freqMap;
    }

    /**
     * Extracts bigrams / key legal phrases from text.
     */
    public Set<String> extractKeyPhrases(String text) {
        List<String> tokens = tokenize(text);
        Set<String> phrases = new HashSet<>(tokens);

        for (int i = 0; i < tokens.size() - 1; i++) {
            phrases.add(tokens.get(i) + " " + tokens.get(i + 1));
        }

        return phrases;
    }

    /**
     * Lightweight rule-based stemmer for legal terminology.
     */
    public String normalizeStem(String word) {
        if (word.length() <= 3) return word;

        if (word.endsWith("ies") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("ing") && word.length() > 5) {
            return word.substring(0, word.length() - 3);
        }
        if (word.endsWith("ence") && word.length() > 6) {
            return word.substring(0, word.length() - 4);
        }
        if (word.endsWith("ance") && word.length() > 6) {
            return word.substring(0, word.length() - 4);
        }
        if (word.endsWith("ment") && word.length() > 6) {
            return word.substring(0, word.length() - 4);
        }
        if (word.endsWith("tion") && word.length() > 6) {
            return word.substring(0, word.length() - 4);
        }
        if (word.endsWith("able") && word.length() > 6) {
            return word.substring(0, word.length() - 4);
        }
        if (word.endsWith("ive") && word.length() > 5) {
            return word.substring(0, word.length() - 3);
        }
        if (word.endsWith("ed") && word.length() > 4) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("s") && !word.endsWith("ss") && word.length() > 3) {
            return word.substring(0, word.length() - 1);
        }

        return word;
    }
}
