package com.aiqaos.core.contract;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class BaseMetadata implements Serializable {
    private UUID requestId;
    private UUID correlationId;
    private String traceId;
    private UUID workflowId;
    private UUID executionId;
    private String sessionId;
    private String tenantId;
    private LocalDateTime timestamp;
    private String source;
    private String version;

    public BaseMetadata() {
        this.timestamp = LocalDateTime.now();
    }

    public UUID getRequestId() { return requestId; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }
    public UUID getCorrelationId() { return correlationId; }
    public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }
    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final BaseMetadata instance = new BaseMetadata();

        public Builder requestId(UUID requestId) { instance.setRequestId(requestId); return this; }
        public Builder correlationId(UUID correlationId) { instance.setCorrelationId(correlationId); return this; }
        public Builder traceId(String traceId) { instance.setTraceId(traceId); return this; }
        public Builder workflowId(UUID workflowId) { instance.setWorkflowId(workflowId); return this; }
        public Builder executionId(UUID executionId) { instance.setExecutionId(executionId); return this; }
        public Builder sessionId(String sessionId) { instance.setSessionId(sessionId); return this; }
        public Builder tenantId(String tenantId) { instance.setTenantId(tenantId); return this; }
        public Builder timestamp(LocalDateTime timestamp) { instance.setTimestamp(timestamp); return this; }
        public Builder source(String source) { instance.setSource(source); return this; }
        public Builder version(String version) { instance.setVersion(version); return this; }

        public BaseMetadata build() { return instance; }
    }
}