package com.aiqaos.core.engine;

import com.aiqaos.core.enums.AgentType;
import java.util.UUID;

public interface AgentManager {
    Agent<?, ?> getAgent(UUID agentId);
    Agent<?, ?> getAgentByType(AgentType type);
    void registerAgent(Agent<?, ?> agent);
    void deregisterAgent(UUID agentId);
}