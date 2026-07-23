package com.aiqaos.orchestration.review;

import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.context.WorkflowContext;

import java.util.UUID;

/**
 * AI-2 — a pipeline run paused for human review (AI-1 HUMAN_REVIEW verdict), retained in memory
 * on the gateway JVM so it can resume from {@code nextStepIndex} with its context intact.
 * Not durable across a gateway restart (F1 = in-memory; durable resume is FI-AI2-A / SCALE-1/2).
 */
public class PausedRun {

    private final UUID workflowId;
    private final UUID executionId;
    private final String correlationId;
    private final WorkflowRequest request;
    private final WorkflowContext context;
    private final int nextStepIndex;
    private final String stepName;
    private final double confidence;
    private final int[] counters; // {success, failed, skipped}

    public PausedRun(UUID workflowId, UUID executionId, String correlationId,
                     WorkflowRequest request, WorkflowContext context,
                     int nextStepIndex, String stepName, double confidence, int[] counters) {
        this.workflowId = workflowId;
        this.executionId = executionId;
        this.correlationId = correlationId;
        this.request = request;
        this.context = context;
        this.nextStepIndex = nextStepIndex;
        this.stepName = stepName;
        this.confidence = confidence;
        this.counters = counters;
    }

    public UUID getWorkflowId() { return workflowId; }
    public UUID getExecutionId() { return executionId; }
    public String getCorrelationId() { return correlationId; }
    public WorkflowRequest getRequest() { return request; }
    public WorkflowContext getContext() { return context; }
    public int getNextStepIndex() { return nextStepIndex; }
    public String getStepName() { return stepName; }
    public double getConfidence() { return confidence; }
    public int[] getCounters() { return counters; }
}
