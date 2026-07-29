package com.aiqaos.provider.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aiqaos.ai.cache")
public class PromptCacheProperties {

    /**
     * Whether semantic prompt caching for AI invocations is enabled.
     */
    private boolean enabled = true;

    /**
     * Cosine similarity threshold (0.0 to 1.0) above which a cached response is returned.
     */
    private double similarityThreshold = 0.95;

    /**
     * Vector store collection name where prompt cache entries are stored.
     */
    private String collection = "prompt_cache";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }
}
