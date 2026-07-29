package com.aiqaos.gateway.audit;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * GOV-1: pure unification — orders the collected {@link AiAuditEvent}s by time and computes rollups.
 * No repository/Spring coupling, so it is fully unit-testable independently of the data sources.
 */
@Component
public class AiAuditAssembler {

    public AiAuditTrail assemble(String workflowId, String correlationId, List<AiAuditEvent> events) {
        List<AiAuditEvent> ordered = events.stream()
                .sorted(Comparator.comparing(AiAuditEvent::getTimestamp,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        double totalCost = ordered.stream()
                .map(AiAuditEvent::getCost)
                .filter(c -> c != null)
                .mapToDouble(Double::doubleValue)
                .sum();

        Map<String, Long> facetCounts = ordered.stream()
                .collect(Collectors.groupingBy(e -> e.getFacet().name(), Collectors.counting()));

        return new AiAuditTrail(workflowId, correlationId, ordered, totalCost, facetCounts);
    }
}
