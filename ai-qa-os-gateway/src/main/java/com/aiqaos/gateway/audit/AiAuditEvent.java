package com.aiqaos.gateway.audit;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * GOV-1: one entry in the unified AI audit trail — a single AI action mapped from a per-domain
 * immutable record (a model call, a cost, an approval, or a security event) into a common shape.
 */
public class AiAuditEvent {

    public enum Facet {
        MODEL_CALL,
        COST,
        APPROVAL,
        SECURITY
    }

    private final Facet facet;
    private final LocalDateTime timestamp;
    private final String actor;
    private final String summary;
    private final Double cost;
    private final Map<String, Object> details;

    public AiAuditEvent(Facet facet, LocalDateTime timestamp, String actor, String summary,
                        Double cost, Map<String, Object> details) {
        this.facet = facet;
        this.timestamp = timestamp;
        this.actor = actor;
        this.summary = summary;
        this.cost = cost;
        this.details = details == null ? Map.of() : details;
    }

    public Facet getFacet() {
        return facet;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getActor() {
        return actor;
    }

    public String getSummary() {
        return summary;
    }

    public Double getCost() {
        return cost;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
