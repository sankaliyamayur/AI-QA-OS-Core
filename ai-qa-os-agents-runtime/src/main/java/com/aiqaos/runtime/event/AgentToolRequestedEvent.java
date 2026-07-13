package com.aiqaos.runtime.event;

import java.util.UUID;

public class AgentToolRequestedEvent {
    private final UUID agentId;
    private final String toolName;
    private final String toolArguments;

    public AgentToolRequestedEvent(UUID agentId, String toolName, String toolArguments) {
        this.agentId = agentId;
        this.toolName = toolName;
        this.toolArguments = toolArguments;
    }

    public UUID getAgentId() { return agentId; }
    public String getToolName() { return toolName; }
    public String getToolArguments() { return toolArguments; }
}
