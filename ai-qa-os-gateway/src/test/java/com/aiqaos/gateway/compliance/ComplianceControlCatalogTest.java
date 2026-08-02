package com.aiqaos.gateway.compliance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * GOV-2 (ADR-081): the curated catalog is well-formed and <b>honest</b> — a control is never marked
 * SATISFIED without naming the capability that satisfies it, and all three frameworks are covered.
 */
class ComplianceControlCatalogTest {

    private final List<ComplianceControl> controls = new ComplianceControlCatalog().controls();

    @Test
    void catalogIsNonEmptyAndWellFormed() {
        assertFalse(controls.isEmpty());
        for (ComplianceControl c : controls) {
            assertTrue(c.id() != null && !c.id().isBlank(), "id");
            assertTrue(c.framework() != null, "framework");
            assertTrue(c.requirementRef() != null && !c.requirementRef().isBlank(), "requirementRef");
            assertTrue(c.status() != null, "status");
        }
    }

    @Test
    void satisfiedControlsNameTheirCapability() {
        for (ComplianceControl c : controls) {
            if (c.status() == ControlStatus.SATISFIED) {
                assertTrue(c.satisfiedBy() != null && !c.satisfiedBy().isBlank(),
                        "SATISFIED control " + c.id() + " must name the capability that satisfies it");
            }
        }
    }

    @Test
    void allThreeFrameworksAreCovered() {
        Set<ComplianceFramework> present = controls.stream()
                .map(ComplianceControl::framework).collect(Collectors.toSet());
        assertTrue(present.contains(ComplianceFramework.SOC2));
        assertTrue(present.contains(ComplianceFramework.ISO_27001));
        assertTrue(present.contains(ComplianceFramework.GDPR));
    }

    @Test
    void containsHonestGaps() {
        // A credible self-attestation is not all-green: some controls are PARTIAL / NOT_IMPLEMENTED.
        assertTrue(controls.stream().anyMatch(c -> c.status() != ControlStatus.SATISFIED),
                "catalog should honestly record gaps, not claim full coverage");
    }
}
