package com.aiqaos.runtime.agent;

import com.aiqaos.agent.model.AgentResultDTO;
import com.aiqaos.runtime.task.AgentTask;
import java.util.UUID;

public class AgentInstance implements AutonomousAgent {
    private final UUID agentId = UUID.randomUUID();
    private String status = "IDLE";

    @Override
    public AgentResultDTO execute(AgentTask task) {
        this.status = "RUNNING";
        AgentResultDTO res = new AgentResultDTO();
        res.setStatus("SUCCESS");
        res.setOutput("Agent instance processed goal: " + task.getGoal());
        this.status = "COMPLETED";
        return res;
    }

    @Override
    public String status() { return status; }

    @Override
    public void shutdown() { this.status = "SHUTDOWN"; }
}