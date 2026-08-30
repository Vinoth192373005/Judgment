package com.legalai.controller;

import com.legalai.model.CourtListenerDTO;
import com.legalai.model.CourtListenerSearchResponse;
import com.legalai.model.LegalCase;
import com.legalai.service.CourtListenerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for live CourtListener API integration and case importation.
 */
@RestController
@RequestMapping("/api/courtlistener")
@CrossOrigin(origins = "*")
public class CourtListenerController {

    private final CourtListenerService courtListenerService;

    public CourtListenerController(CourtListenerService courtListenerService) {
        this.courtListenerService = courtListenerService;
    }

    /**
     * Searches opinions from CourtListener API in real-time.
     */
    @GetMapping("/search")
    public ResponseEntity<CourtListenerSearchResponse> searchOpinions(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "page", defaultValue = "1") int page) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        CourtListenerSearchResponse response = courtListenerService.searchOpinions(query.trim(), page);
        return ResponseEntity.ok(response);
    }

    /**
     * Imports a CourtListener case directly into the Supabase database and indexes it.
     */
    @PostMapping("/import")
    public ResponseEntity<LegalCase> importCase(@RequestBody CourtListenerDTO dto) {
        try {
            LegalCase importedCase = courtListenerService.importCase(dto);
            return ResponseEntity.ok(importedCase);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
