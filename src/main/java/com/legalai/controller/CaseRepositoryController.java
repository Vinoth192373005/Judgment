package com.legalai.controller;

import com.legalai.model.*;
import com.legalai.service.CaseRepositoryService;
import com.legalai.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for Case Repository operations (Module 1).
 */
@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class CaseRepositoryController {

    private final CaseRepositoryService caseRepositoryService;
    private final RecommendationService recommendationService;

    public CaseRepositoryController(CaseRepositoryService caseRepositoryService, RecommendationService recommendationService) {
        this.caseRepositoryService = caseRepositoryService;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<List<LegalCase>> getAllOrSearchCases(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) LegalDomain domain,
            @RequestParam(required = false) CourtLevel courtLevel,
            @RequestParam(required = false) CaseOutcome outcome,
            @RequestParam(required = false) Boolean landmarkOnly,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) String statute,
            @RequestParam(required = false) String judge
    ) {
        CaseSearchCriteria criteria = new CaseSearchCriteria();
        criteria.setQuery(query);
        criteria.setDomain(domain);
        criteria.setCourtLevel(courtLevel);
        criteria.setOutcome(outcome);
        criteria.setLandmarkOnly(landmarkOnly);
        criteria.setStartYear(startYear);
        criteria.setEndYear(endYear);
        criteria.setStatute(statute);
        criteria.setJudge(judge);

        return ResponseEntity.ok(caseRepositoryService.searchCases(criteria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LegalCase> getCaseById(@PathVariable Long id) {
        return caseRepositoryService.getCaseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LegalCase> createCase(@Valid @RequestBody LegalCase legalCase) {
        LegalCase saved = caseRepositoryService.createCase(legalCase);
        recommendationService.reindexCorpus();
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LegalCase> updateCase(@PathVariable Long id, @Valid @RequestBody LegalCase updatedCase) {
        try {
            LegalCase saved = caseRepositoryService.updateCase(id, updatedCase);
            recommendationService.reindexCorpus();
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCase(@PathVariable Long id) {
        try {
            caseRepositoryService.deleteCase(id);
            recommendationService.reindexCorpus();
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/landmarks")
    public ResponseEntity<List<LegalCase>> getLandmarkCases() {
        return ResponseEntity.ok(caseRepositoryService.getLandmarkCases());
    }

    @GetMapping("/domains")
    public ResponseEntity<List<Map<String, String>>> getDomains() {
        List<Map<String, String>> domains = new ArrayList<>();
        for (LegalDomain d : LegalDomain.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("name", d.name());
            map.put("displayName", d.getDisplayName());
            map.put("description", d.getDescription());
            domains.add(map);
        }
        return ResponseEntity.ok(domains);
    }

    @GetMapping("/courts")
    public ResponseEntity<List<Map<String, Object>>> getCourts() {
        List<Map<String, Object>> courts = new ArrayList<>();
        for (CourtLevel c : CourtLevel.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", c.name());
            map.put("displayName", c.getDisplayName());
            map.put("weight", c.getPrecedentWeight());
            map.put("description", c.getDescription());
            courts.add(map);
        }
        return ResponseEntity.ok(courts);
    }

    @GetMapping("/outcomes")
    public ResponseEntity<List<Map<String, String>>> getOutcomes() {
        List<Map<String, String>> outcomes = new ArrayList<>();
        for (CaseOutcome o : CaseOutcome.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("name", o.name());
            map.put("displayName", o.getDisplayName());
            map.put("description", o.getDescription());
            outcomes.add(map);
        }
        return ResponseEntity.ok(outcomes);
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importCases(@RequestBody List<LegalCase> cases) {
        List<LegalCase> saved = caseRepositoryService.importCases(cases);
        recommendationService.reindexCorpus();
        Map<String, Object> response = new HashMap<>();
        response.put("importedCount", saved.size());
        response.put("message", "Successfully imported and indexed " + saved.size() + " legal cases.");
        return ResponseEntity.ok(response);
    }
}
