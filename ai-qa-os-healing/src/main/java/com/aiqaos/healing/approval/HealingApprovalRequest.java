package com.aiqaos.healing.approval;

/**
 * HEAL-2: a record of a heal awaiting (or resolved through) the approval workflow — what would
 * change ({@code summary}), the healing confidence, and the current {@link HealingApprovalStatus}.
 * Mutable status so the lifecycle (pending → approved/rejected) can be recorded.
 */
public final class HealingApprovalRequest {

    private final String healingId;
    private final String summary;
    private final double confidence;
    private final String correlationId;
    private final long sequence;
    private HealingApprovalStatus status;

    public HealingApprovalRequest(String healingId, String summary, double confidence,
                                  String correlationId, long sequence, HealingApprovalStatus status) {
        this.healingId = healingId;
        this.summary = summary;
        this.confidence = confidence;
        this.correlationId = correlationId;
        this.sequence = sequence;
        this.status = status;
    }

    public String getHealingId() { return healingId; }
    public String getSummary() { return summary; }
    public double getConfidence() { return confidence; }
    public String getCorrelationId() { return correlationId; }
    public long getSequence() { return sequence; }
    public HealingApprovalStatus getStatus() { return status; }
    public void setStatus(HealingApprovalStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "HealingApprovalRequest{" + healingId + " [" + status + "] " + summary + "}";
    }
}
