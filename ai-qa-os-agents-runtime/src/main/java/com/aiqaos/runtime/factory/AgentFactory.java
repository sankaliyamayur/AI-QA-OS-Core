package com.aiqaos.runtime.factory;

import com.aiqaos.runtime.agent.AutonomousAgent;
import com.aiqaos.runtime.agent.AgentInstance;
import org.springframework.stereotype.Component;

@Component
public class AgentFactory {
    public AutonomousAgent createAgent(String type) {
        return new AgentInstance();
    }
}