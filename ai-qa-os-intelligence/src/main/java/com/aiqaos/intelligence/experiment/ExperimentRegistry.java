package com.aiqaos.intelligence.experiment;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * PE-2: the set of active prompt experiments, keyed by {@code promptRef}. In-memory for now; durable
 * experiment storage is FI-PE2-B. Prompt resolution consults {@link #experimentFor} and, when present,
 * routes via {@link PromptExperimentRouter} (that wiring is the deferred live integration, FI-PE2-A).
 */
@Component
public class ExperimentRegistry {

    private final Map<String, PromptExperiment> byPromptRef = new ConcurrentHashMap<>();

    public void register(PromptExperiment experiment) {
        if (experiment != null && experiment.getPromptRef() != null) {
            byPromptRef.put(experiment.getPromptRef(), experiment);
        }
    }

    public Optional<PromptExperiment> experimentFor(String promptRef) {
        return promptRef == null ? Optional.empty() : Optional.ofNullable(byPromptRef.get(promptRef));
    }
}
