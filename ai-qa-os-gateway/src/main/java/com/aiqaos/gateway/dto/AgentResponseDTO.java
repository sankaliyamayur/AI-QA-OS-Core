package com.aiqaos.gateway.dto;

public class AgentResponseDTO extends GatewayResponseDTO {
    private String agentId;
    private String agentType;
    private String currentTask;

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
    public String getCurrentTask() { return currentTask; }
    public void setCurrentTask(String currentTask) { this.currentTask = currentTask; }
}