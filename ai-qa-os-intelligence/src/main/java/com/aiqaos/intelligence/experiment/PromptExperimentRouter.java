package com.aiqaos.intelligence.experiment;

import org.springframework.stereotype.Component;

/**
 * PE-2: deterministic, weighted A/B assignment (§0.4-A). A stable {@code key} (e.g. the workflow
 * {@code correlationId}) hashes into the variants' cumulative-weight ranges, so the same key always
 * resolves to the same variant while the weight split holds in aggregate. A disabled/empty experiment
 * returns the default version — pure routing, no side effects.
 */
@Component
public class PromptExperimentRouter {

    /** The prompt {@code versionId} to serve for {@code key} under {@code experiment}. */
    public String assign(PromptExperiment experiment, String key) {
        if (experiment == null) {
            return null;
        }
        if (!experiment.isEnabled() || experiment.getVariants().isEmpty()) {
            return experiment.getDefaultVersionId();
        }

        int totalWeight = experiment.getVariants().stream()
                .mapToInt(PromptExperiment.Variant::getWeight)
                .sum();
        if (totalWeight <= 0) {
            return experiment.getDefaultVersionId();
        }

        int bucket = Math.floorMod(key == null ? 0 : key.hashCode(), totalWeight);
        int cumulative = 0;
        for (PromptExperiment.Variant variant : experiment.getVariants()) {
            cumulative += variant.getWeight();
            if (bucket < cumulative) {
                return variant.getVersionId();
            }
        }
        // Unreachable when weights are positive; safe fallback to the last variant.
        return experiment.getVariants().get(experiment.getVariants().size() - 1).getVersionId();
    }
}
