package com.aiqaos.dashboard.controller;

import com.aiqaos.workflow.model.ExecutionHistoryDTO;
import com.aiqaos.workflow.service.WorkflowExecutionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard/executions")
public class ExecutionHistoryController {

    private final WorkflowExecutionService workflowExecutionService;

    public ExecutionHistoryController(WorkflowExecutionService workflowExecutionService) {
        this.workflowExecutionService = workflowExecutionService;
    }

    @GetMapping
    public Page<ExecutionHistoryDTO> search(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "gitBranch", required = false) String gitBranch,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {
        return workflowExecutionService.search(status, gitBranch, from, to, pageable);
    }

    @GetMapping("/{executionId}")
    public ResponseEntity<ExecutionHistoryDTO> getByExecutionId(@PathVariable("executionId") UUID executionId) {
        return workflowExecutionService.getByExecutionId(executionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
