package com.aiqaos.agent.event;

import com.aiqaos.core.event.AgentEvent;

public class AgentFailedEvent extends AgentEvent {
    public AgentFailedEvent(String agentName, String reason) {
        setAgentName(agentName);
        setActionTaken("FAILED: " + reason);
    }
}