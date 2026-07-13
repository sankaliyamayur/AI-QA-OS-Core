package com.aiqaos.execution.event;

import java.util.UUID;

public class ExecutionFailedEvent {
    private final UUID executionId;
    private final String reason;

    public ExecutionFailedEvent(UUID executionId, String reason) {
        this.executionId = executionId;
        this.reason = reason;
    }

    public UUID getExecutionId() { return executionId; }
    public String getReason() { return reason; }
}