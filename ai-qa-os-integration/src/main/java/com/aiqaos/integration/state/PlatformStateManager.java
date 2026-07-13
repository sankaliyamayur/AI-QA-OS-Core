package com.aiqaos.integration.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlatformStateManager {
    private static final Logger log = LoggerFactory.getLogger(PlatformStateManager.class);
    private final ConcurrentHashMap<String, PlatformState> states = new ConcurrentHashMap<>();

    public void transitionTo(String workflowId, PlatformState state) {
        states.put(workflowId, state);
        log.info("Workflow ID: {} transitioned to state: {}", workflowId, state);
    }

    public PlatformState getCurrentState(String workflowId) {
        return states.getOrDefault(workflowId, PlatformState.REQUEST_RECEIVED);
    }
}