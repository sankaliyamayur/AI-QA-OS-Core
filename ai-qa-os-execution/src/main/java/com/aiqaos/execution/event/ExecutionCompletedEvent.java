package com.aiqaos.execution.event;

import java.util.UUID;

public class ExecutionCompletedEvent {
    private final UUID executionId;

    public ExecutionCompletedEvent(UUID executionId) {
        this.executionId = executionId;
    }

    public UUID getExecutionId() { return executionId; }
}