package com.aiqaos.gateway.controller;

import com.aiqaos.gateway.dto.HumanReviewDTO;
import com.aiqaos.gateway.dto.ReviewDecisionDTO;
import com.aiqaos.gateway.dto.WorkflowResponseDTO;
import com.aiqaos.gateway.dto.WorkflowStartRequestDTO;
import com.aiqaos.gateway.service.WorkflowGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {

    private final WorkflowGatewayService workflowGatewayService;

    public WorkflowController(WorkflowGatewayService workflowGatewayService) {
        this.workflowGatewayService = workflowGatewayService;
    }

    @PostMapping("/start")
    public ResponseEntity<WorkflowResponseDTO> start(@RequestBody WorkflowStartRequestDTO request) {
        return ResponseEntity.ok(workflowGatewayService.start(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowResponseDTO> getStatus(@PathVariable String id) {
        return ResponseEntity.ok(workflowGatewayService.getStatus(id));
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<WorkflowResponseDTO> pause(@PathVariable String id) {
        return ResponseEntity.ok(workflowGatewayService.pause(id));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<WorkflowResponseDTO> resume(@PathVariable String id) {
        return ResponseEntity.ok(workflowGatewayService.resume(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<WorkflowResponseDTO> cancel(@PathVariable String id) {
        return ResponseEntity.ok(workflowGatewayService.cancel(id));
    }

    // AI-2: human-in-the-loop approval resource
    @GetMapping("/reviews")
    public ResponseEntity<List<HumanReviewDTO>> reviews() {
        return ResponseEntity.ok(workflowGatewayService.listReviews());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<WorkflowResponseDTO> approve(@PathVariable String id,
                                                       @RequestBody(required = false) ReviewDecisionDTO decision) {
        String reviewer = decision != null ? decision.getReviewer() : null;
        String comment = decision != null ? decision.getComment() : null;
        return ResponseEntity.ok(workflowGatewayService.approve(id, reviewer, comment));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<WorkflowResponseDTO> reject(@PathVariable String id,
                                                      @RequestBody(required = false) ReviewDecisionDTO decision) {
        String reviewer = decision != null ? decision.getReviewer() : null;
        String comment = decision != null ? decision.getComment() : null;
        return ResponseEntity.ok(workflowGatewayService.reject(id, reviewer, comment));
    }
}