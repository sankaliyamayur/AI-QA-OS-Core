package com.aiqaos.workflow.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.UUID;

public class WorkflowResponseDTO implements BaseDTO {
    private UUID executionId;
    private String status;
    private String resultSummary;

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; }
}