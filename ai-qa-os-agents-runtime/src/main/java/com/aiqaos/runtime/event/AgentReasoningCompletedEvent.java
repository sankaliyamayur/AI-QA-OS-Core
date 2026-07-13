package com.aiqaos.runtime.event;

import java.util.UUID;

public class AgentReasoningCompletedEvent {
    private final UUID agentId;
    private final String decision;
    private final double confidence;

    public AgentReasoningCompletedEvent(UUID agentId, String decision, double confidence) {
        this.agentId = agentId;
        this.decision = decision;
        this.confidence = confidence;
    }

    public UUID getAgentId() { return agentId; }
    public String getDecision() { return decision; }
    public double getConfidence() { return confidence; }
}
