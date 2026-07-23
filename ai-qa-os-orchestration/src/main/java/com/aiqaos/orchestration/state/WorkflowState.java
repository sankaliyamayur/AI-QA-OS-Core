package com.aiqaos.orchestration.state;

public enum WorkflowState {
    CREATED,
    VALIDATED,
    READY,
    RUNNING,
    PAUSED,
    RESUMED,
    COMPLETED,
    FAILED,
    CANCELLED
}