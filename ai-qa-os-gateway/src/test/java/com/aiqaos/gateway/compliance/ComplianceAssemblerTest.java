package com.aiqaos.gateway.compliance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.gateway.compliance.ComplianceReport.ComplianceFrameworkSummary;
import java.util.List;
import org.junit.jupiter.api.Test;

/** GOV-2 (ADR-081): per-framework grouping + honest coverage math (satisfied / total). */
class ComplianceAssemblerTest {

    private final ComplianceAssembler assembler = new ComplianceAssembler();

    private static ComplianceControl control(ComplianceFramework f, String ref, ControlStatus status) {
        return new ComplianceControl(f + "-" + ref, f, ref, "t", status == ControlStatus.NOT_IMPLEMENTED ? "" : "cap",
                status, "e");
    }

    @Test
    void computesCoverageAndCountsPerFramework() {
        // SOC2: 2 satisfied, 1 partial, 1 not-impl of 4 -> coverage 50.0
        ComplianceReport report = assembler.assemble(List.of(
                control(ComplianceFramework.SOC2, "CC6.1", ControlStatus.SATISFIED),
                control(ComplianceFramework.SOC2, "CC6.2", ControlStatus.SATISFIED),
                control(ComplianceFramework.SOC2, "CC6.7", ControlStatus.PARTIAL),
                control(ComplianceFramework.SOC2, "CC7.1", ControlStatus.NOT_IMPLEMENTED),
                control(ComplianceFramework.GDPR, "Art.32", ControlStatus.SATISFIED)));

        assertEquals(2, report.frameworks().size(), "SOC2 + GDPR present, ISO absent");
        ComplianceFrameworkSummary soc2 = summary(report, ComplianceFramework.SOC2);
        assertEquals(4, soc2.total());
        assertEquals(2, soc2.satisfied());
        assertEquals(1, soc2.partial());
        assertEquals(1, soc2.notImplemented());
        assertEquals(50.0, soc2.coveragePercent(), 1e-9);
        assertEquals(100.0, summary(report, ComplianceFramework.GDPR).coveragePercent(), 1e-9);
    }

    @Test
    void controlsSortedByRequirementRef() {
        ComplianceReport report = assembler.assemble(List.of(
                control(ComplianceFramework.SOC2, "CC8.1", ControlStatus.SATISFIED),
                control(ComplianceFramework.SOC2, "CC6.1", ControlStatus.SATISFIED)));
        assertEquals("CC6.1", summary(report, ComplianceFramework.SOC2).controls().get(0).requirementRef());
    }

    @Test
    void emptyOrNull_yieldsNoFrameworks() {
        assertTrue(assembler.assemble(List.of()).frameworks().isEmpty());
        assertTrue(assembler.assemble(null).frameworks().isEmpty());
    }

    private static ComplianceFrameworkSummary summary(ComplianceReport r, ComplianceFramework f) {
        return r.frameworks().stream().filter(s -> s.framework() == f).findFirst().orElseThrow();
    }
}
