package com.aiqaos.gateway.client;

import com.aiqaos.gateway.dto.WorkflowResponseDTO;
import com.aiqaos.gateway.dto.WorkflowStartRequestDTO;

public interface WorkflowClient {
    WorkflowResponseDTO start(WorkflowStartRequestDTO request);
    WorkflowResponseDTO getStatus(String workflowId);
    WorkflowResponseDTO pause(String workflowId);
    WorkflowResponseDTO resume(String workflowId);
    WorkflowResponseDTO cancel(String workflowId);
}