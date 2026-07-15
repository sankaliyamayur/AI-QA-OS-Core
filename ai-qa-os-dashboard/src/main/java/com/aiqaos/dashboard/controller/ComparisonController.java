package com.aiqaos.dashboard.controller;

import com.aiqaos.workflow.model.ExecutionComparisonDTO;
import com.aiqaos.workflow.service.WorkflowExecutionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard/compare")
public class ComparisonController {

    private final WorkflowExecutionService workflowExecutionService;

    public ComparisonController(WorkflowExecutionService workflowExecutionService) {
        this.workflowExecutionService = workflowExecutionService;
    }

    @GetMapping
    public ExecutionComparisonDTO compare(@RequestParam("id1") UUID id1, @RequestParam("id2") UUID id2) {
        return workflowExecutionService.compare(id1, id2);
    }
}
