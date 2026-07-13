package com.aiqaos.gateway.dto;

public class ExecutionRequestDTO extends GatewayRequestDTO {
    private String workflowId;
    private String environment;
    private String browser;

    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
}