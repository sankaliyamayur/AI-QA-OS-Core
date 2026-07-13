package com.aiqaos.workflow.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.ArrayList;
import java.util.List;

public class WorkflowResultDTO implements BaseDTO {
    private String status;
    private List<WorkflowStepResultDTO> stepResults = new ArrayList<>();

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<WorkflowStepResultDTO> getStepResults() { return stepResults; }
    public void setStepResults(List<WorkflowStepResultDTO> stepResults) { this.stepResults = stepResults; }
}