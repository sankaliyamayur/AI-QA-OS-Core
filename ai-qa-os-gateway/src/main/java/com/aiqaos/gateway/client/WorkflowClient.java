package com.aiqaos.gateway.client;

import com.aiqaos.gateway.dto.HumanReviewDTO;
import com.aiqaos.gateway.dto.WorkflowResponseDTO;
import com.aiqaos.gateway.dto.WorkflowStartRequestDTO;

import java.util.List;

public interface WorkflowClient {
    WorkflowResponseDTO start(WorkflowStartRequestDTO request);
    WorkflowResponseDTO getStatus(String workflowId);
    WorkflowResponseDTO pause(String workflowId);
    WorkflowResponseDTO resume(String workflowId);
    WorkflowResponseDTO cancel(String workflowId);

    // AI-2: human-in-the-loop approval resource
    List<HumanReviewDTO> listReviews();
    WorkflowResponseDTO approve(String workflowId, String reviewer, String comment);
    WorkflowResponseDTO reject(String workflowId, String reviewer, String comment);
}
