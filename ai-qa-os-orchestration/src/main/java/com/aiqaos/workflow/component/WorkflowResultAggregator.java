package com.aiqaos.workflow.component;

import com.aiqaos.workflow.model.WorkflowResultDTO;
import com.aiqaos.workflow.model.WorkflowStepResultDTO;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class WorkflowResultAggregator {
    public WorkflowResultDTO aggregate(List<WorkflowStepResultDTO> results) {
        WorkflowResultDTO aggregated = new WorkflowResultDTO();
        aggregated.setStepResults(results);
        boolean anyFailed = results.stream().anyMatch(r -> "FAILED".equals(r.getStatus()));
        aggregated.setStatus(anyFailed ? "FAILED" : "SUCCESS");
        return aggregated;
    }
}