package com.aiqaos.agent.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.UUID;

public class AgentResultDTO implements BaseDTO {
    private UUID executionId;
    private UUID agentId;
    private String status;
    private String output;
    private String metadata;
    private long executionTime;

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public UUID getAgentId() { return agentId; }
    public void setAgentId(UUID agentId) { this.agentId = agentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
}