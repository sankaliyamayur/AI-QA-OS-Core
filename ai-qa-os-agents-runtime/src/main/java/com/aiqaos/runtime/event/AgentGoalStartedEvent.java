package com.aiqaos.runtime.event;

import java.util.UUID;

public class AgentGoalStartedEvent {
    private final UUID agentId;
    private final String goal;

    public AgentGoalStartedEvent(UUID agentId, String goal) {
        this.agentId = agentId;
        this.goal = goal;
    }

    public UUID getAgentId() { return agentId; }
    public String getGoal() { return goal; }
}
