package com.aiqaos.gateway.service;

import com.aiqaos.gateway.client.WorkflowClient;
import com.aiqaos.gateway.dto.HumanReviewDTO;
import com.aiqaos.gateway.dto.WorkflowResponseDTO;
import com.aiqaos.gateway.dto.WorkflowStartRequestDTO;
import com.aiqaos.gateway.event.GatewayEventPublisher;
import com.aiqaos.security.ratelimit.RateLimiter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowGatewayService extends GatewayService {

    private final WorkflowClient workflowClient;

    public WorkflowGatewayService(WorkflowClient workflowClient,
                                   GatewayEventPublisher eventPublisher,
                                   RateLimiter rateLimiter) {
        super(eventPublisher, rateLimiter);
        this.workflowClient = workflowClient;
    }

    public WorkflowResponseDTO start(WorkflowStartRequestDTO request) {
        log.info("Starting workflow: {}", request.getWorkflowName());
        return workflowClient.start(request);
    }

    public WorkflowResponseDTO getStatus(String workflowId)  { return workflowClient.getStatus(workflowId); }
    public WorkflowResponseDTO pause(String workflowId)      { return workflowClient.pause(workflowId); }
    public WorkflowResponseDTO resume(String workflowId)     { return workflowClient.resume(workflowId); }
    public WorkflowResponseDTO cancel(String workflowId)     { return workflowClient.cancel(workflowId); }

    // AI-2: human-in-the-loop approval resource
    public List<HumanReviewDTO> listReviews()                                     { return workflowClient.listReviews(); }
    public WorkflowResponseDTO approve(String id, String reviewer, String comment) { return workflowClient.approve(id, reviewer, comment); }
    public WorkflowResponseDTO reject(String id, String reviewer, String comment)  { return workflowClient.reject(id, reviewer, comment); }
}