package com.aiqaos.integration.recovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RecoveryManager {
    private static final Logger log = LoggerFactory.getLogger(RecoveryManager.class);
    private final ConcurrentHashMap<String, ExecutionCheckpoint> checkpoints = new ConcurrentHashMap<>();

    public void saveCheckpoint(String workflowId, String stepName, com.aiqaos.integration.state.PlatformState state) {
        checkpoints.put(workflowId, new ExecutionCheckpoint(stepName, state));
        log.info("Saved checkpoint for Workflow {}: step={}, state={}", workflowId, stepName, state);
    }

    public boolean recover(String workflowId) {
        ExecutionCheckpoint checkpoint = checkpoints.get(workflowId);
        if (checkpoint != null) {
            log.warn("Recovering Workflow {} from checkpoint: step={}, state={}", 
                workflowId, checkpoint.getStepName(), checkpoint.getState());
            return true;
        }
        return false;
    }
}