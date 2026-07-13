package com.aiqaos.workflow.model;

import com.aiqaos.core.dto.BaseDTO;

public class WorkflowStepResultDTO implements BaseDTO {
    private String stepId;
    private String status;
    private String executionOutput;

    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getExecutionOutput() { return executionOutput; }
    public void setExecutionOutput(String executionOutput) { this.executionOutput = executionOutput; }
}