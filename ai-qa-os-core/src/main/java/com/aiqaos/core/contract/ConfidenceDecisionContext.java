package com.aiqaos.core.contract;

/**
 * AI-1 — inputs to a single confidence-gate evaluation: which pipeline step produced the
 * output, the confidence it reported, and the correlation id for traceability.
 */
public class ConfidenceDecisionContext {

    private final String stepName;
    private final double confidence;
    private final String correlationId;

    public ConfidenceDecisionContext(String stepName, double confidence, String correlationId) {
        this.stepName = stepName;
        this.confidence = confidence;
        this.correlationId = correlationId;
    }

    public String getStepName() { return stepName; }
    public double getConfidence() { return confidence; }
    public String getCorrelationId() { return correlationId; }
}
