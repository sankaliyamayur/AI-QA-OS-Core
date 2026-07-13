package com.aiqaos.execution.model;

import com.aiqaos.core.dto.BaseDTO;

public class ExecutionStepResultDTO implements BaseDTO {
    private String stepName;
    private String status;

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}