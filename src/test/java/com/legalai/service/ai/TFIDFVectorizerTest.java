package com.legalai.service.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TFIDFVectorizerTest {

    private TextProcessor textProcessor;
    private TFIDFVectorizer vectorizer;

    @BeforeEach
    void setUp() {
        textProcessor = new TextProcessor();
        vectorizer = new TFIDFVectorizer(textProcessor);
    }

    @Test
    void testTokenizationAndStemming() {
        String text = "The defendant committed intentional copyright infringement and negligence under statutory law.";
        List<String> tokens = textProcessor.tokenize(text);

        assertNotNull(tokens);
        assertTrue(tokens.contains("copyright"));
        assertTrue(tokens.contains("infringe") || tokens.contains("infring"));
        assertTrue(tokens.contains("neglig") || tokens.contains("negligence"));
        assertFalse(tokens.contains("the")); // stop-word
        assertFalse(tokens.contains("and")); // stop-word
    }

    @Test
    void testVectorizationAndCosineSimilarity() {
        List<String> corpus = Arrays.asList(
                "Breach of software license contract and unauthorized commercial distribution of source code",
                "Criminal prosecution for homicide, robbery and possession of illegal firearms under penal code",
                "Patent infringement regarding mRNA lipid nanoparticle delivery systems in vaccine biotechnology"
        );

        Map<String, Double> idf = vectorizer.computeIDF(corpus);
        assertNotNull(idf);
        assertFalse(idf.isEmpty());

        Map<String, Double> queryVector = vectorizer.vectorize("Software source code license breach and piracy", idf);
        Map<String, Double> doc1Vector = vectorizer.vectorize(corpus.get(0), idf);
        Map<String, Double> doc2Vector = vectorizer.vectorize(corpus.get(1), idf);

        double sim1 = vectorizer.cosineSimilarity(queryVector, doc1Vector);
        double sim2 = vectorizer.cosineSimilarity(queryVector, doc2Vector);

        assertTrue(sim1 > sim2, "Query about software breach should be much more similar to Doc 1 than Doc 2");
        assertTrue(sim1 > 0.3, "Sim1 should have strong positive cosine similarity");
    }

    @Test
    void testStatuteOverlapComputation() {
        List<String> queryStatutes = Arrays.asList("Section 302 IPC", "Article 21");
        String caseStatutes = "Section 302 IPC, Section 120B IPC, Indian Evidence Act";

        double overlap = vectorizer.computeStatuteOverlap(queryStatutes, caseStatutes);
        assertTrue(overlap > 0.4, "Statute overlap should recognize shared Section 302 IPC");
    }
}
