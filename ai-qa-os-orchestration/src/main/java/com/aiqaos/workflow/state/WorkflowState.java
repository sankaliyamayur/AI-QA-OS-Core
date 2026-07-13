package com.aiqaos.workflow.state;

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