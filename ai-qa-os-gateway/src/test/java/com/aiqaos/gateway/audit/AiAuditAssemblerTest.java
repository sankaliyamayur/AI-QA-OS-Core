package com.aiqaos.gateway.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** GOV-1: the pure unification — time ordering + rollups across facets. */
class AiAuditAssemblerTest {

    private final AiAuditAssembler assembler = new AiAuditAssembler();

    private static AiAuditEvent event(AiAuditEvent.Facet facet, int minute, Double cost) {
        return new AiAuditEvent(facet, LocalDateTime.of(2026, 7, 28, 10, minute), "actor",
                facet.name(), cost, Map.of());
    }

    @Test
    void ordersByTimestampAndRollsUpCostAndFacetCounts() {
        List<AiAuditEvent> unordered = List.of(
                event(AiAuditEvent.Facet.COST, 30, 0.25),
                event(AiAuditEvent.Facet.MODEL_CALL, 10, null),
                event(AiAuditEvent.Facet.APPROVAL, 40, null),
                event(AiAuditEvent.Facet.COST, 20, 0.75),
                event(AiAuditEvent.Facet.SECURITY, 15, null));

        AiAuditTrail trail = assembler.assemble("wf-1", "corr-1", unordered);

        // time-ordered: 10, 15, 20, 30, 40
        assertThat(trail.getEvents()).extracting(e -> e.getTimestamp().getMinute())
                .containsExactly(10, 15, 20, 30, 40);
        assertThat(trail.getTotalCost()).isEqualTo(1.00);
        assertThat(trail.getFacetCounts())
                .containsEntry("COST", 2L)
                .containsEntry("MODEL_CALL", 1L)
                .containsEntry("APPROVAL", 1L)
                .containsEntry("SECURITY", 1L);
        assertThat(trail.getWorkflowId()).isEqualTo("wf-1");
        assertThat(trail.getCorrelationId()).isEqualTo("corr-1");
    }

    @Test
    void emptyTrailIsWellFormed() {
        AiAuditTrail trail = assembler.assemble("wf-1", null, List.of());

        assertThat(trail.getEvents()).isEmpty();
        assertThat(trail.getTotalCost()).isEqualTo(0.0);
        assertThat(trail.getFacetCounts()).isEmpty();
    }
}
