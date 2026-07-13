package com.aiqaos.gateway.dto;

public class AgentStartRequestDTO extends GatewayRequestDTO {
    private String agentType;
    private String task;

    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }
}