package com.aiqaos.gateway.audit;

import java.util.List;
import java.util.Map;

/**
 * GOV-1: the assembled AI audit trail for one workflow run — its AI actions in time order, plus
 * rollups (total cost, count per facet).
 */
public class AiAuditTrail {

    private final String workflowId;
    private final String correlationId;
    private final List<AiAuditEvent> events;
    private final double totalCost;
    private final Map<String, Long> facetCounts;

    public AiAuditTrail(String workflowId, String correlationId, List<AiAuditEvent> events,
                        double totalCost, Map<String, Long> facetCounts) {
        this.workflowId = workflowId;
        this.correlationId = correlationId;
        this.events = events == null ? List.of() : events;
        this.totalCost = totalCost;
        this.facetCounts = facetCounts == null ? Map.of() : facetCounts;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public List<AiAuditEvent> getEvents() {
        return events;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public Map<String, Long> getFacetCounts() {
        return facetCounts;
    }
}
