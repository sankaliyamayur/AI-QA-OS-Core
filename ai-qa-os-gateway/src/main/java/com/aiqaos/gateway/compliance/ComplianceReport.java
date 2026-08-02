package com.aiqaos.gateway.compliance;

import java.util.List;

/**
 * GOV-2 (ADR-081): the compliance read-model — a self-attestation disclaimer plus per-framework
 * coverage. Composed by {@link ComplianceAssembler} from the {@link ComplianceControlCatalog}.
 */
public record ComplianceReport(
        String attestation,
        List<ComplianceFrameworkSummary> frameworks) {

    /** Per-framework coverage over its controls. */
    public record ComplianceFrameworkSummary(
            ComplianceFramework framework,
            int total,
            int satisfied,
            int partial,
            int notImplemented,
            double coveragePercent,   // satisfied / total, one decimal
            List<ComplianceControl> controls) {
    }
}
