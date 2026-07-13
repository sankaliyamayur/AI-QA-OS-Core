package com.aiqaos.runtime.event;

import java.util.UUID;

public class AgentLifecycleEvent {
    private final UUID agentId;
    private final String state;

    public AgentLifecycleEvent(UUID agentId, String state) {
        this.agentId = agentId;
        this.state = state;
    }

    public UUID getAgentId() { return agentId; }
    public String getState() { return state; }
}