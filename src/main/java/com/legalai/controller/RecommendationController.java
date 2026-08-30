package com.legalai.controller;

import com.legalai.model.RecommendationRequest;
import com.legalai.model.RecommendationResult;
import com.legalai.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for AI Judgment Recommendation and Outcome Prediction (Module 2).
 */
@RestController
@RequestMapping("/api/recommendation")
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<RecommendationResult> analyzeAndRecommend(@Valid @RequestBody RecommendationRequest request) {
        RecommendationResult result = recommendationService.recommend(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reindex")
    public ResponseEntity<Map<String, String>> reindexVectors() {
        recommendationService.reindexCorpus();
        Map<String, String> res = new HashMap<>();
        res.put("status", "SUCCESS");
        res.put("message", "AI Corpus vectors and IDF indices refreshed successfully.");
        return ResponseEntity.ok(res);
    }
}
