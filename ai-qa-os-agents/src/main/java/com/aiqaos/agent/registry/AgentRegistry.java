package com.aiqaos.agent.registry;

import com.aiqaos.core.engine.Agent;
import com.aiqaos.core.enums.AgentType;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentRegistry {
    private final Map<UUID, Agent<?, ?>> registry = new ConcurrentHashMap<>();
    private final Map<AgentType, Agent<?, ?>> typedRegistry = new ConcurrentHashMap<>();

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        Map<String, Agent> agentBeans = event.getApplicationContext().getBeansOfType(Agent.class);
        for (Agent<?, ?> agent : agentBeans.values()) {
            register(UUID.randomUUID(), agent);
        }
    }

    public void register(UUID agentId, Agent<?, ?> agent) {
        registry.put(agentId, agent);
        typedRegistry.put(agent.getType(), agent);
    }

    public Agent<?, ?> getAgent(UUID agentId) {
        return registry.get(agentId);
    }

    public Agent<?, ?> getAgentByType(AgentType type) {
        return typedRegistry.get(type);
    }

    public void remove(UUID agentId) {
        Agent<?, ?> agent = registry.remove(agentId);
        if (agent != null) {
            typedRegistry.remove(agent.getType());
        }
    }
}