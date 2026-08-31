package com.legalai.config;

import com.legalai.repository.LegalCaseRepository;
import com.legalai.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Startup component to initialize and index the database vector space for AI precedent matching.
 * Uses only database records and CourtListener API data.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final LegalCaseRepository caseRepository;
    private final RecommendationService recommendationService;

    public DataLoader(LegalCaseRepository caseRepository, 
                      RecommendationService recommendationService) {
        this.caseRepository = caseRepository;
        this.recommendationService = recommendationService;
    }

    @Override
    public void run(String... args) {
        long count = caseRepository.count();
        log.info("Initializing Legal AI System. Total cases in database: {}.", count);

        if (count > 0) {
            recommendationService.reindexCorpus();
            log.info("TF-IDF Vector Corpus indexing complete with {} active authorities.", count);
        } else {
            log.info("Database is currently empty. Cases will be populated via CourtListener API queries and user entries.");
        }
    }
}
