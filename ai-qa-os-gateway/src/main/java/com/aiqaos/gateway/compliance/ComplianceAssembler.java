package com.aiqaos.gateway.compliance;

import com.aiqaos.gateway.compliance.ComplianceReport.ComplianceFrameworkSummary;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * GOV-2 (ADR-081): composes the {@link ComplianceControlCatalog} into a per-framework
 * {@link ComplianceReport} (coverage = satisfied / total). Pure — no I/O — the GOV-1/HEAL-3 assembler
 * pattern. Coverage counts only {@code SATISFIED} controls (partials are reported separately), so the
 * headline number is honest rather than inflated.
 */
@Component
public class ComplianceAssembler {

    static final String ATTESTATION =
            "Self-attested mapping of implemented platform capabilities to framework requirements. "
                    + "Not a formal certification or external audit; statuses reflect shipped capabilities.";

    public ComplianceReport assemble(List<ComplianceControl> controls) {
        List<ComplianceFrameworkSummary> summaries = new ArrayList<>();
        if (controls != null) {
            for (ComplianceFramework framework : ComplianceFramework.values()) {
                List<ComplianceControl> forFramework = controls.stream()
                        .filter(c -> c.framework() == framework)
                        .sorted(Comparator.comparing(ComplianceControl::requirementRef))
                        .toList();
                if (forFramework.isEmpty()) {
                    continue;
                }
                int total = forFramework.size();
                int satisfied = (int) forFramework.stream().filter(c -> c.status() == ControlStatus.SATISFIED).count();
                int partial = (int) forFramework.stream().filter(c -> c.status() == ControlStatus.PARTIAL).count();
                int notImplemented = total - satisfied - partial;
                double coverage = total == 0 ? 0.0 : Math.round((satisfied * 1000.0) / total) / 10.0;
                summaries.add(new ComplianceFrameworkSummary(
                        framework, total, satisfied, partial, notImplemented, coverage, forFramework));
            }
        }
        return new ComplianceReport(ATTESTATION, summaries);
    }
}
