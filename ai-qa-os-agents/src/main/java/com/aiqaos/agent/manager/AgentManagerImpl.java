package com.aiqaos.agent.manager;

import com.aiqaos.core.engine.Agent;
import com.aiqaos.core.engine.AgentManager;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.agent.registry.AgentRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AgentManagerImpl implements AgentManager {

    @Autowired
    private AgentRegistry agentRegistry;

    @Override
    public Agent<?, ?> getAgent(UUID agentId) {
        return agentRegistry.getAgent(agentId);
    }

    @Override
    public Agent<?, ?> getAgentByType(AgentType type) {
        return agentRegistry.getAgentByType(type);
    }

    @Override
    public void registerAgent(Agent<?, ?> agent) {
        agentRegistry.register(UUID.randomUUID(), agent);
    }

    @Override
    public void deregisterAgent(UUID agentId) {
        agentRegistry.remove(agentId);
    }
}