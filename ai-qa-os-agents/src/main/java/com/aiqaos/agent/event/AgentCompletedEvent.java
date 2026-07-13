package com.aiqaos.agent.event;

import com.aiqaos.core.event.AgentEvent;

public class AgentCompletedEvent extends AgentEvent {
    public AgentCompletedEvent(String agentName) {
        setAgentName(agentName);
        setActionTaken("COMPLETED");
    }
}