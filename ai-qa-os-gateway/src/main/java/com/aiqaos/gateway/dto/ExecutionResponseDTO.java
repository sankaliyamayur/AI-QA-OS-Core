package com.aiqaos.gateway.dto;

public class ExecutionResponseDTO extends GatewayResponseDTO {
    private String executionId;
    private String executionStatus;
    private String artifactsUrl;

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public String getExecutionStatus() { return executionStatus; }
    public void setExecutionStatus(String executionStatus) { this.executionStatus = executionStatus; }
    public String getArtifactsUrl() { return artifactsUrl; }
    public void setArtifactsUrl(String artifactsUrl) { this.artifactsUrl = artifactsUrl; }
}