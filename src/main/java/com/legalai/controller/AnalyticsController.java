package com.legalai.controller;

import com.legalai.model.AnalyticsSummary;
import com.legalai.model.CaseComparisonResponse;
import com.legalai.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Judicial Analytics and Dashboards (Module 3).
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummary> getAnalyticsSummary() {
        return ResponseEntity.ok(analyticsService.getAnalyticsSummary());
    }

    @PostMapping("/compare")
    public ResponseEntity<CaseComparisonResponse> compareCases(@RequestBody List<Long> caseIds) {
        return ResponseEntity.ok(analyticsService.compareCases(caseIds));
    }
}
