package com.aiqaos.execution.event;

import java.util.UUID;

public class ExecutionStartedEvent {
    private final UUID executionId;

    public ExecutionStartedEvent(UUID executionId) {
        this.executionId = executionId;
    }

    public UUID getExecutionId() { return executionId; }
}