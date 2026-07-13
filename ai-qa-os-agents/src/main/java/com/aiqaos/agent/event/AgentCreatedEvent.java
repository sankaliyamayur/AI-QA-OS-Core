package com.aiqaos.agent.event;

import com.aiqaos.core.event.AgentEvent;

public class AgentCreatedEvent extends AgentEvent {
    public AgentCreatedEvent(String agentName) {
        setAgentName(agentName);
        setActionTaken("CREATED");
    }
}