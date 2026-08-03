package com.aiqaos.gateway.client;

import com.aiqaos.core.contract.BaseMetadata;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.gateway.dto.HumanReviewDTO;
import com.aiqaos.gateway.dto.WorkflowResponseDTO;
import com.aiqaos.gateway.dto.WorkflowStartRequestDTO;
import com.aiqaos.orchestration.entity.HumanReviewEntity;
import com.aiqaos.orchestration.pipeline.AutonomousQAPipelineOrchestrator;
import com.aiqaos.orchestration.review.HumanReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class WorkflowClientImpl implements WorkflowClient {

    private static final Logger log = LoggerFactory.getLogger(WorkflowClientImpl.class);

    private final AutonomousQAPipelineOrchestrator orchestrator;
    private final HumanReviewService humanReviewService;

    public WorkflowClientImpl(AutonomousQAPipelineOrchestrator orchestrator, HumanReviewService humanReviewService) {
        this.orchestrator = orchestrator;
        this.humanReviewService = humanReviewService;
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
        } else {
            // The start API accepts requests without a correlationId; default one so the
            // downstream metadata (and the response's correlationId.toString()) never NPEs.
            metadata.setCorrelationId(UUID.randomUUID());
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

    @Override public WorkflowResponseDTO getStatus(String workflowId) {
        WorkflowResponseDTO r = new WorkflowResponseDTO();
        r.setWorkflowId(workflowId);
        r.setWorkflowStatus("UNKNOWN"); // status is authoritative in workflow_executions (dashboard reads it)
        return r;
    }

    @Override public WorkflowResponseDTO pause(String workflowId) { return new WorkflowResponseDTO(); }

    // AI-2: resume/cancel delegate to the approval flow (system reviewer for the bare control endpoints).
    @Override public WorkflowResponseDTO resume(String workflowId) { return approve(workflowId, "system", null); }
    @Override public WorkflowResponseDTO cancel(String workflowId) { return reject(workflowId, "system", null); }

    @Override
    public List<HumanReviewDTO> listReviews() {
        return humanReviewService.listPending().stream().map(WorkflowClientImpl::toDto).toList();
    }

    @Override
    public WorkflowResponseDTO approve(String workflowId, String reviewer, String comment) {
        UUID wf = UUID.fromString(workflowId);
        String who = reviewer != null ? reviewer : "system";
        // Resume runs the remaining pipeline in the background, like start().
        CompletableFuture.runAsync(() -> {
            try {
                orchestrator.resume(wf, who, comment);
            } catch (Exception e) {
                log.error("Resume failed for workflowId={}", workflowId, e);
            }
        });
        WorkflowResponseDTO r = new WorkflowResponseDTO();
        r.setWorkflowId(workflowId);
        r.setWorkflowStatus("RESUMING");
        r.setMessage("Approved by " + who + "; resuming pipeline from the paused step.");
        return r;
    }

    @Override
    public WorkflowResponseDTO reject(String workflowId, String reviewer, String comment) {
        UUID wf = UUID.fromString(workflowId);
        WorkflowResponse resp = orchestrator.reject(wf, reviewer != null ? reviewer : "system", comment);
        WorkflowResponseDTO r = new WorkflowResponseDTO();
        r.setWorkflowId(workflowId);
        r.setWorkflowStatus(resp.getStatus());
        r.setMessage(resp.getMessage());
        return r;
    }

    private static HumanReviewDTO toDto(HumanReviewEntity e) {
        HumanReviewDTO d = new HumanReviewDTO();
        d.setReviewId(e.getReviewId() != null ? e.getReviewId().toString() : null);
        d.setWorkflowId(e.getWorkflowId() != null ? e.getWorkflowId().toString() : null);
        d.setExecutionId(e.getExecutionId() != null ? e.getExecutionId().toString() : null);
        d.setStepName(e.getStepName());
        d.setConfidence(e.getConfidence());
        d.setStatus(e.getStatus());
        d.setCreatedTime(e.getCreatedTime() != null ? e.getCreatedTime().toString() : null);
        return d;
    }
}