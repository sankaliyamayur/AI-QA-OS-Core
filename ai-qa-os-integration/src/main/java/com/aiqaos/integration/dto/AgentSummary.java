package com.aiqaos.integration.dto;

public class AgentSummary {
    private final String agentName;
    private final String taskPurpose;
    private final String outcome;

    public AgentSummary(String agentName, String taskPurpose, String outcome) {
        this.agentName = agentName;
        this.taskPurpose = taskPurpose;
        this.outcome = outcome;
    }

    public String getAgentName() { return agentName; }
    public String getTaskPurpose() { return taskPurpose; }
    public String getOutcome() { return outcome; }
}