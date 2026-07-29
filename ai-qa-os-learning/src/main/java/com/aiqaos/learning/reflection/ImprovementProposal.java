package com.aiqaos.learning.reflection;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * LRN-1: a concrete, typed improvement derived from a root-caused {@code FailurePattern} — the
 * output of the loop's improvement stage. A proposal is <em>recorded</em>, not applied: adoption is
 * gated on LRN-4's safe-adoption gate (FI-LRN1-A).
 */
public class ImprovementProposal implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Priority of acting on the proposal (recurring / high-confidence patterns are HIGH). */
    public enum Priority { HIGH, NORMAL }

    private String proposalId;
    private ProposalType type;
    private String targetComponent;
    private String rationale;
    private String sourcePatternId;
    private double confidence;
    private boolean recurring;
    private Priority priority = Priority.NORMAL;
    private LocalDateTime createdAt = LocalDateTime.now();

    public ImprovementProposal() {
    }

    public ImprovementProposal(String proposalId, ProposalType type, String targetComponent,
                               String rationale, String sourcePatternId, double confidence,
                               boolean recurring, Priority priority) {
        this.proposalId = proposalId;
        this.type = type;
        this.targetComponent = targetComponent;
        this.rationale = rationale;
        this.sourcePatternId = sourcePatternId;
        this.confidence = confidence;
        this.recurring = recurring;
        this.priority = priority;
    }

    public String getProposalId() { return proposalId; }
    public void setProposalId(String proposalId) { this.proposalId = proposalId; }

    public ProposalType getType() { return type; }
    public void setType(ProposalType type) { this.type = type; }

    public String getTargetComponent() { return targetComponent; }
    public void setTargetComponent(String targetComponent) { this.targetComponent = targetComponent; }

    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }

    public String getSourcePatternId() { return sourcePatternId; }
    public void setSourcePatternId(String sourcePatternId) { this.sourcePatternId = sourcePatternId; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "ImprovementProposal{" + type + " → " + targetComponent + " [" + priority + "]"
                + (recurring ? " recurring" : "") + ": " + rationale + "}";
    }
}
