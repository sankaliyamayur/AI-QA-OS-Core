package com.aiqaos.gateway.client;

import com.aiqaos.core.contract.BaseMetadata;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.gateway.dto.WorkflowResponseDTO;
import com.aiqaos.gateway.dto.WorkflowStartRequestDTO;
import com.aiqaos.workflow.pipeline.AutonomousQAPipelineOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class WorkflowClientImpl implements WorkflowClient {

    private static final Logger log = LoggerFactory.getLogger(WorkflowClientImpl.class);

    private final AutonomousQAPipelineOrchestrator orchestrator;

    public WorkflowClientImpl(AutonomousQAPipelineOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public WorkflowResponseDTO start(WorkflowStartRequestDTO request) {
        UUID workflowId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();

        // 1. Build request
        WorkflowRequest req = new WorkflowRequest();
        req.setWorkflowName(request.getWorkflowName());
        if (request.getParameters() != null) {
            req.setInputs(request.getParameters());
        }

        // Align keys: copy requirementPath to storyPath if present
        if (req.getInputs().containsKey("requirementPath") && !req.getInputs().containsKey("storyPath")) {
            req.getInputs().put("storyPath", req.getInputs().get("requirementPath"));
        }

        // Set metadata
        BaseMetadata metadata = req.getMetadata();
        metadata.setWorkflowId(workflowId);
        metadata.setExecutionId(executionId);
        if (request.getCorrelationId() != null) {
            try {
                metadata.setCorrelationId(UUID.fromString(request.getCorrelationId()));
            } catch (IllegalArgumentException e) {
                metadata.setCorrelationId(UUID.randomUUID());
            }
        }

        // 2. Build context
        WorkflowContext context = new WorkflowContext();
        context.getMetadata().setWorkflowId(workflowId);
        context.getMetadata().setExecutionId(executionId);
        context.getMetadata().setCorrelationId(metadata.getCorrelationId());

        // 3. Run asynchronously in background
        CompletableFuture.runAsync(() -> {
            try {
                orchestrator.runPipeline(req, context);
            } catch (Exception e) {
                log.error("Pipeline execution failed for workflowId={}, correlationId={}",
                        workflowId, metadata.getCorrelationId(), e);
            }
        });

        // 4. Return DTO response immediately
        WorkflowResponseDTO r = new WorkflowResponseDTO();
        r.setWorkflowId(workflowId.toString());
        r.setWorkflowStatus("STARTED");
        r.setCorrelationId(metadata.getCorrelationId().toString());
        r.setMessage("Autonomous QA Pipeline has started in the background.");
        return r;
    }

    @Override public WorkflowResponseDTO getStatus(String workflowId) { return new WorkflowResponseDTO(); }
    @Override public WorkflowResponseDTO pause(String workflowId)     { return new WorkflowResponseDTO(); }
    @Override public WorkflowResponseDTO resume(String workflowId)    { return new WorkflowResponseDTO(); }
    @Override public WorkflowResponseDTO cancel(String workflowId)    { return new WorkflowResponseDTO(); }
}