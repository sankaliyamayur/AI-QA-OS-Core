package com.aiqaos.orchestration.review;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI-2 — in-memory registry of runs paused for human review, keyed by workflow id.
 * Lives in the JVM that runs the pipeline (the gateway); resume looks the run up here.
 */
@Component
public class PausedWorkflowRegistry {

    private final Map<UUID, PausedRun> paused = new ConcurrentHashMap<>();

    public void register(PausedRun run) {
        paused.put(run.getWorkflowId(), run);
    }

    public PausedRun get(UUID workflowId) {
        return paused.get(workflowId);
    }

    public PausedRun remove(UUID workflowId) {
        return paused.remove(workflowId);
    }

    public boolean contains(UUID workflowId) {
        return paused.containsKey(workflowId);
    }
}
