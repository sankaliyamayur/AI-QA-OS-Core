package com.aiqaos.gateway.compliance;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GOV-2 (ADR-081): serves the compliance read-model — the platform's control catalog mapped to
 * SOC 2 / ISO 27001 / GDPR, with per-framework coverage. Read-only; a self-attestation, not a
 * certification. Sits on the gateway's enforced chain (authenticated when security is on).
 */
@RestController
@RequestMapping("/api/governance/compliance")
public class ComplianceController {

    private final ComplianceControlCatalog catalog;
    private final ComplianceAssembler assembler;

    public ComplianceController(ComplianceControlCatalog catalog, ComplianceAssembler assembler) {
        this.catalog = catalog;
        this.assembler = assembler;
    }

    @GetMapping
    public ComplianceReport report() {
        return assembler.assemble(catalog.controls());
    }
}
