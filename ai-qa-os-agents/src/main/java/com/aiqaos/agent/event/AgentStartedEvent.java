package com.aiqaos.agent.event;

import com.aiqaos.core.event.AgentEvent;

public class AgentStartedEvent extends AgentEvent {
    public AgentStartedEvent(String agentName) {
        setAgentName(agentName);
        setActionTaken("STARTED");
    }
}