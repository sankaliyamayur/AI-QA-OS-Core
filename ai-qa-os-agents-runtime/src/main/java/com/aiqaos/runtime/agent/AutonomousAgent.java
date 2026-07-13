package com.aiqaos.runtime.agent;

import com.aiqaos.agent.model.AgentResultDTO;
import com.aiqaos.runtime.task.AgentTask;

public interface AutonomousAgent {
    AgentResultDTO execute(AgentTask task);
    String status();
    void shutdown();
}