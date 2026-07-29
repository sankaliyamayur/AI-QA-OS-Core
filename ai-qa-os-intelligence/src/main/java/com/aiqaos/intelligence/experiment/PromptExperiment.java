package com.aiqaos.intelligence.experiment;

import java.util.ArrayList;
import java.util.List;

/**
 * PE-2: an A/B experiment over a logical prompt ({@code promptRef}) — a set of weighted variants
 * (each a prompt {@code versionId}) plus a default used when the experiment is disabled/empty.
 */
public class PromptExperiment {

    /** A single weighted variant: a prompt version and its traffic weight. */
    public static class Variant {
        private final String versionId;
        private final int weight;

        public Variant(String versionId, int weight) {
            this.versionId = versionId;
            this.weight = weight;
        }

        public String getVersionId() {
            return versionId;
        }

        public int getWeight() {
            return weight;
        }
    }

    private final String experimentId;
    private final String promptRef;
    private final String defaultVersionId;
    private final List<Variant> variants;
    private final boolean enabled;

    public PromptExperiment(String experimentId, String promptRef, String defaultVersionId,
                            List<Variant> variants, boolean enabled) {
        this.experimentId = experimentId;
        this.promptRef = promptRef;
        this.defaultVersionId = defaultVersionId;
        this.variants = variants == null ? new ArrayList<>() : new ArrayList<>(variants);
        this.enabled = enabled;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getPromptRef() {
        return promptRef;
    }

    public String getDefaultVersionId() {
        return defaultVersionId;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
