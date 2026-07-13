package com.aiqaos.integration.context;

import java.util.UUID;

public class IntegrationContext {
    private final UUID correlationId;
    private final UUID workflowId;
    private final UUID executionId;
    private final String userId;
    private final String traceId;

    public IntegrationContext(UUID correlationId, UUID workflowId, UUID executionId, String userId, String traceId) {
        this.correlationId = correlationId;
        this.workflowId = workflowId;
        this.executionId = executionId;
        this.userId = userId;
        this.traceId = traceId;
    }

    public UUID getCorrelationId() { return correlationId; }
    public UUID getWorkflowId() { return workflowId; }
    public UUID getExecutionId() { return executionId; }
    public String getUserId() { return userId; }
    public String getTraceId() { return traceId; }
}