package com.aiqaos.gateway.client;

import com.aiqaos.gateway.dto.WorkflowResponseDTO;
import com.aiqaos.gateway.dto.WorkflowStartRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class WorkflowClientImpl implements WorkflowClient {

    @Override
    public WorkflowResponseDTO start(WorkflowStartRequestDTO request) {
        // TODO: delegate to WorkflowOrchestrator
        WorkflowResponseDTO r = new WorkflowResponseDTO();
        r.setWorkflowStatus("STARTED");
        return r;
    }

    @Override public WorkflowResponseDTO getStatus(String workflowId) { return new WorkflowResponseDTO(); }
    @Override public WorkflowResponseDTO pause(String workflowId)     { return new WorkflowResponseDTO(); }
    @Override public WorkflowResponseDTO resume(String workflowId)    { return new WorkflowResponseDTO(); }
    @Override public WorkflowResponseDTO cancel(String workflowId)    { return new WorkflowResponseDTO(); }
}