package com.aiqaos.core.event;

public class AgentEvent extends BaseEvent {
    private String agentName;
    private String actionTaken;

    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }
}