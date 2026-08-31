package com.legalai.service.ai;

import com.legalai.model.LegalDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextProcessorTest {

    private TextProcessor textProcessor;

    @BeforeEach
    void setUp() {
        textProcessor = new TextProcessor();
    }

    @Test
    void testInferDomainCriminalTheft() {
        LegalDomain domain = textProcessor.inferDomain("car theft and stolen vehicle recovered by police", Collections.emptyList());
        assertEquals(LegalDomain.CRIMINAL, domain);
    }

    @Test
    void testInferDomainCriminalIPCStatute() {
        LegalDomain domain = textProcessor.inferDomain("The accused was apprehended fleeing with stolen property", List.of("Section 379 IPC"));
        assertEquals(LegalDomain.CRIMINAL, domain);
    }

    @Test
    void testInferDomainCivilTortAccident() {
        LegalDomain domain = textProcessor.inferDomain("Motor vehicle collision and property damage with insurance surveyor dispute", List.of("Motor Vehicles Act § 166"));
        assertEquals(LegalDomain.CIVIL_TORT, domain);
    }

    @Test
    void testInferDomainConstitutionalBasicStructure() {
        LegalDomain domain = textProcessor.inferDomain("Basic structure doctrine and Parliament constitutional amending power judicial review", List.of("Article 368"));
        assertEquals(LegalDomain.CONSTITUTIONAL, domain);
    }

    @Test
    void testInferDomainCyberPrivacy() {
        LegalDomain domain = textProcessor.inferDomain("Mass biometric surveillance telemetry and warrantless electronic interception infringing privacy", List.of("Section 69 IT Act"));
        assertEquals(LegalDomain.CYBER_DEFAMATION, domain);
    }

    @Test
    void testInferDomainIntellectualProperty() {
        LegalDomain domain = textProcessor.inferDomain("Training artificial intelligence diffusion model on copyrighted software code and API declarations", List.of("17 U.S. Code § 107"));
        assertEquals(LegalDomain.INTELLECTUAL_PROPERTY, domain);
    }

    @Test
    void testInferDomainCorporateCommercial() {
        LegalDomain domain = textProcessor.inferDomain("Breach of commercial supply agreement and liquidated damages deduction under arbitral award", List.of("Section 74 Contract Act"));
        assertEquals(LegalDomain.CORPORATE_COMMERCIAL, domain);
    }

    @Test
    void testInferDomainLaborEmployment() {
        LegalDomain domain = textProcessor.inferDomain("Rideshare platform gig economy drivers claiming statutory minimum wage and holiday pay", Collections.emptyList());
        assertEquals(LegalDomain.LABOR_EMPLOYMENT, domain);
    }

    @Test
    void testInferDomainEnvironmental() {
        LegalDomain domain = textProcessor.inferDomain("Toxic oleum gas leak from industrial fertilizer plant causing absolute liability and severe pollution", Collections.emptyList());
        assertEquals(LegalDomain.ENVIRONMENTAL, domain);
    }
}
