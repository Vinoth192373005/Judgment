package com.legalai.service.ai;

import com.legalai.model.LegalDomain;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Natural Language Processing component for legal text tokenization,
 * legal stop-word filtering, term normalization, keyphrase extraction,
 * and domain classification.
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
     * Extracts clean, concise search keywords suitable for querying CourtListener and database repository.
     */
    public String extractSearchKeywords(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String clean = text.trim();
        // If short, return as is
        if (clean.length() <= 50) {
            return clean;
        }
        // Take first sentence or up to first punctuation
        int punctIdx = -1;
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (c == '.' || c == '\n' || c == ';') {
                punctIdx = i;
                break;
            }
        }
        if (punctIdx > 0 && punctIdx <= 60) {
            return clean.substring(0, punctIdx).trim();
        }
        // Extract top tokens
        List<String> tokens = tokenize(clean);
        if (tokens.isEmpty()) {
            return clean.substring(0, Math.min(50, clean.length())).trim();
        }
        int count = Math.min(5, tokens.size());
        return String.join(" ", tokens.subList(0, count));
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
     * Infers the most appropriate LegalDomain from factual text and statutory citations.
     */
    public LegalDomain inferDomain(String text, List<String> statutes) {
        StringBuilder combined = new StringBuilder();
        if (text != null) combined.append(text.toLowerCase(Locale.ROOT)).append(" ");
        if (statutes != null) {
            for (String s : statutes) {
                if (s != null) combined.append(s.toLowerCase(Locale.ROOT)).append(" ");
            }
        }
        String corpus = combined.toString();

        Map<LegalDomain, Double> domainScores = new EnumMap<>(LegalDomain.class);
        for (LegalDomain d : LegalDomain.values()) {
            domainScores.put(d, 0.0);
        }

        // 1. Criminal Law
        scoreKeywords(domainScores, LegalDomain.CRIMINAL, corpus,
                new String[]{"theft", "car theft", "stolen", "vehicle theft", "robbery", "dacoity", "burglary",
                        "larceny", "extortion", "murder", "homicide", "ipc 302", "ipc 378", "ipc 379", "section 378",
                        "section 379", "section 411", "section 420", "stolen vehicle", "fir", "police", "accused",
                        "convict", "bail", "charge sheet", "penal code", "criminal", "assault", "narcotics", "ndps",
                        "crpc", "acquittal", "custodial", "culpable homicide", "death penalty", "rarest of rare"},
                new double[]{6.0, 12.0, 6.0, 12.0, 6.0, 6.0, 6.0,
                        6.0, 6.0, 6.0, 6.0, 8.0, 8.0, 8.0, 8.0,
                        8.0, 8.0, 8.0, 10.0, 4.0, 3.0, 3.0,
                        3.0, 4.0, 4.0, 4.0, 3.0, 3.0, 4.0, 4.0,
                        3.0, 3.0, 4.0, 6.0, 6.0, 6.0});

        // 2. Civil & Tort Law
        scoreKeywords(domainScores, LegalDomain.CIVIL_TORT, corpus,
                new String[]{"motor accident", "vehicular accident", "vehicle damage", "motor vehicle", "mv act",
                        "insurance claim", "surveyor", "repudiation", "own damage", "third party", "negligence",
                        "medical negligence", "malpractice", "doctor", "hospital", "consumer protection", "deficiency in service",
                        "personal injury", "tort", "compensation", "multiplier", "fatal accident", "collision"},
                new double[]{8.0, 8.0, 8.0, 6.0, 6.0,
                        6.0, 5.0, 5.0, 6.0, 5.0, 4.0,
                        8.0, 6.0, 3.0, 3.0, 7.0, 7.0,
                        5.0, 4.0, 3.0, 5.0, 6.0, 5.0});

        // 3. Cyber & Media Law (Privacy / Intermediary)
        scoreKeywords(domainScores, LegalDomain.CYBER_DEFAMATION, corpus,
                new String[]{"privacy", "right to privacy", "surveillance", "biometric", "aadhaar", "interception",
                        "wiretap", "cyber", "it act", "section 66a", "section 69", "defamation", "libel", "slander",
                        "data protection", "gdpr", "social media", "intermediary liability", "encryption", "informational privacy"},
                new double[]{6.0, 9.0, 7.0, 7.0, 7.0, 6.0,
                        6.0, 5.0, 6.0, 8.0, 8.0, 6.0, 6.0, 6.0,
                        6.0, 6.0, 5.0, 7.0, 6.0, 8.0});

        // 4. Constitutional Law
        scoreKeywords(domainScores, LegalDomain.CONSTITUTIONAL, corpus,
                new String[]{"fundamental right", "article 21", "article 19", "article 14", "article 32", "article 226",
                        "article 368", "basic structure", "writ", "habeas corpus", "mandamus", "certiorari",
                        "judicial review", "constitutional", "unconstitutional", "ultra vires", "state action", "proportionality standard"},
                new double[]{7.0, 7.0, 7.0, 7.0, 7.0, 7.0,
                        8.0, 9.0, 5.0, 6.0, 6.0, 6.0,
                        6.0, 4.0, 5.0, 5.0, 4.0, 6.0});

        // 5. Intellectual Property
        scoreKeywords(domainScores, LegalDomain.INTELLECTUAL_PROPERTY, corpus,
                new String[]{"copyright", "patent", "trademark", "fair use", "infringement", "software", "api",
                        "declaring code", "licensing", "trade secret", "intellectual property", "royalty", "section 107", "transformative"},
                new double[]{7.0, 7.0, 7.0, 7.0, 5.0, 5.0, 6.0,
                        8.0, 4.0, 6.0, 7.0, 5.0, 7.0, 6.0});

        // 6. Corporate & Commercial
        scoreKeywords(domainScores, LegalDomain.CORPORATE_COMMERCIAL, corpus,
                new String[]{"contract", "breach of contract", "liquidated damages", "section 73", "section 74",
                        "contract act", "arbitration", "arbitral award", "shareholder", "merger", "acquisition",
                        "insolvency", "ibc", "promissory", "indemnity", "commercial contract", "consequential damages", "remoteness"},
                new double[]{4.0, 7.0, 8.0, 7.0, 7.0,
                        6.0, 6.0, 6.0, 5.0, 5.0, 5.0,
                        6.0, 6.0, 4.0, 4.0, 6.0, 6.0, 6.0});

        // 7. Labor & Employment
        scoreKeywords(domainScores, LegalDomain.LABOR_EMPLOYMENT, corpus,
                new String[]{"worker", "employee", "employment", "wage", "minimum wage", "salary", "overtime",
                        "labor", "labour", "trade union", "wrongful termination", "dismissal", "gig economy", "rideshare", "holiday pay"},
                new double[]{4.0, 4.0, 4.0, 5.0, 7.0, 4.0, 5.0,
                        4.0, 4.0, 5.0, 7.0, 5.0, 7.0, 6.0, 6.0});

        // 8. Environmental
        scoreKeywords(domainScores, LegalDomain.ENVIRONMENTAL, corpus,
                new String[]{"environment", "pollution", "gas leak", "hazardous", "absolute liability", "toxic",
                        "effluent", "emission", "ngt", "green tribunal", "air pollution", "water pollution", "polluter pays"},
                new double[]{5.0, 6.0, 8.0, 6.0, 8.0, 6.0,
                        6.0, 6.0, 6.0, 6.0, 6.0, 6.0, 7.0});

        // 9. Tax & Financial
        scoreKeywords(domainScores, LegalDomain.TAX_FINANCIAL, corpus,
                new String[]{"income tax", "gst", "customs", "excise", "revenue", "tax evasion", "assessment",
                        "securities", "sebi", "money laundering", "pmla", "financial regulation"},
                new double[]{7.0, 7.0, 6.0, 6.0, 4.0, 7.0, 5.0,
                        5.0, 6.0, 7.0, 7.0, 5.0});

        // 10. Family & Estate
        scoreKeywords(domainScores, LegalDomain.FAMILY_ESTATE, corpus,
                new String[]{"divorce", "custody", "matrimonial", "maintenance", "alimony", "inheritance",
                        "succession", "probate", "will", "ancestral property", "matrimonial property"},
                new double[]{7.0, 7.0, 6.0, 6.0, 6.0, 6.0,
                        6.0, 7.0, 5.0, 7.0, 6.0});

        // Find domain with maximum score
        Map.Entry<LegalDomain, Double> best = Collections.max(domainScores.entrySet(), Map.Entry.comparingByValue());
        if (best.getValue() > 0.0) {
            return best.getKey();
        }

        return null;
    }

    private void scoreKeywords(Map<LegalDomain, Double> domainScores, LegalDomain domain, String corpus, String[] keywords, double[] weights) {
        double current = domainScores.getOrDefault(domain, 0.0);
        for (int i = 0; i < keywords.length; i++) {
            String kw = keywords[i].toLowerCase(Locale.ROOT);
            if (corpus.contains(kw)) {
                current += weights[i];
            }
        }
        domainScores.put(domain, current);
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
