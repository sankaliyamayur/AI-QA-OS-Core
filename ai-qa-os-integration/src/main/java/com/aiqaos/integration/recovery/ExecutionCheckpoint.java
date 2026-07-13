package com.aiqaos.integration.recovery;

import com.aiqaos.integration.state.PlatformState;

public class ExecutionCheckpoint {
    private final String stepName;
    private final PlatformState state;
    private final long timestamp = System.currentTimeMillis();

    public ExecutionCheckpoint(String stepName, PlatformState state) {
        this.stepName = stepName;
        this.state = state;
    }

    public String getStepName() { return stepName; }
    public PlatformState getState() { return state; }
    public long getTimestamp() { return timestamp; }
}