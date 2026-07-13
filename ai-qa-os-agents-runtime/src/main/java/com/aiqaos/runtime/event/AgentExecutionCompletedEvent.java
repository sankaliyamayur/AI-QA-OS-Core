package com.aiqaos.runtime.event;

import java.util.UUID;

public class AgentExecutionCompletedEvent {
    private final UUID agentId;
    private final String status;
    private final long executionTime;

    public AgentExecutionCompletedEvent(UUID agentId, String status, long executionTime) {
        this.agentId = agentId;
        this.status = status;
        this.executionTime = executionTime;
    }

    public UUID getAgentId() { return agentId; }
    public String getStatus() { return status; }
    public long getExecutionTime() { return executionTime; }
}
