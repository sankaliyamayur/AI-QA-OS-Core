package com.aiqaos.provider.cache;

import com.aiqaos.memory.embedding.BatchEmbeddingService;
import com.aiqaos.memory.model.MemoryMetadata;
import com.aiqaos.memory.model.VectorSearchResult;
import com.aiqaos.memory.vector.VectorStoreClient;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component("llmSemanticCacheManager")
public class LlmSemanticCacheManager {

    private static final Logger log = LoggerFactory.getLogger(LlmSemanticCacheManager.class);

    private final PromptCacheProperties properties;
    private final VectorStoreClient vectorStoreClient;
    // PERF-2: Optional — memory module may not be in same app context (e.g. ai-provider unit tests)
    private final ObjectProvider<BatchEmbeddingService> batchEmbeddingServiceProvider;

    @Autowired
    public LlmSemanticCacheManager(PromptCacheProperties properties,
                                   VectorStoreClient vectorStoreClient,
                                   ObjectProvider<BatchEmbeddingService> batchEmbeddingServiceProvider) {
        this.properties = properties;
        this.vectorStoreClient = vectorStoreClient;
        this.batchEmbeddingServiceProvider = batchEmbeddingServiceProvider;
    }

    /** Backward-compatible 2-arg constructor for tests that don't inject BatchEmbeddingService. */
    LlmSemanticCacheManager(PromptCacheProperties properties, VectorStoreClient vectorStoreClient) {
        this.properties = properties;
        this.vectorStoreClient = vectorStoreClient;
        this.batchEmbeddingServiceProvider = null;
    }

    public Optional<LLMResponse> findCachedResponse(LLMRequest request) {
        if (!properties.isEnabled() || request == null || request.getPrompt() == null || request.getPrompt().isBlank()) {
            return Optional.empty();
        }

        try {
            float[] queryEmbedding = computePromptEmbedding(request.getPrompt());
            List<VectorSearchResult> results = vectorStoreClient.search(
                    queryEmbedding,
                    1,
                    properties.getCollection(),
                    new MemoryMetadata()
            );

            if (!results.isEmpty()) {
                VectorSearchResult bestMatch = results.get(0);
                if (bestMatch.getSimilarity() >= properties.getSimilarityThreshold()) {
                    log.info("AI-4: Prompt cache HIT! Similarity: {} (Threshold: {})",
                            bestMatch.getSimilarity(), properties.getSimilarityThreshold());
                    
                    LLMResponse cachedResponse = new LLMResponse(
                            bestMatch.getNode().getContent(),
                            "prompt-cache",
                            new TokenUsage(0, 0), // 0 tokens used on cache hit
                            1L                    // 1ms cached latency
                    );
                    return Optional.of(cachedResponse);
                }
            }
        } catch (Exception e) {
            log.warn("AI-4: Prompt cache lookup error: {}", e.getMessage());
        }

        return Optional.empty();
    }

    public void cacheResponse(LLMRequest request, LLMResponse response) {
        if (!properties.isEnabled() || request == null || request.getPrompt() == null || response == null) {
            return;
        }

        try {
            String cacheId = computeHash(request.getPrompt());
            float[] embedding = computePromptEmbedding(request.getPrompt());
            MemoryMetadata metadata = new MemoryMetadata();
            metadata.getAttributes().put("purpose", request.getPurpose() != null ? request.getPurpose() : "general");
            metadata.getAttributes().put("model", response.getModel());

            vectorStoreClient.save(
                    cacheId,
                    embedding,
                    metadata,
                    response.getText(),
                    properties.getCollection()
            );
            log.debug("AI-4: Cached response for prompt ID: {}", cacheId);
        } catch (Exception e) {
            log.warn("AI-4: Failed to cache prompt response: {}", e.getMessage());
        }
    }

    private float[] computePromptEmbedding(String prompt) {
        // PERF-2: delegate to BatchEmbeddingService when available (reuses cache layer)
        if (batchEmbeddingServiceProvider != null) {
            BatchEmbeddingService batchEmbeddingService = batchEmbeddingServiceProvider.getIfAvailable();
            if (batchEmbeddingService != null) {
                List<float[]> result = batchEmbeddingService.embedBatch(List.of(prompt));
                if (!result.isEmpty()) return result.get(0);
            }
        }
        // Fallback: deterministic feature-embedding (same algorithm, no external dep)
        int dimensions = 128;
        float[] vector = new float[dimensions];
        byte[] bytes = prompt.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            int idx = Math.abs(bytes[i] * 31 + i) % dimensions;
            vector[idx] += (bytes[i] & 0xFF) / 255.0f;
        }
        float norm = 0.0f;
        for (float v : vector) norm += v * v;
        if (norm > 0) {
            norm = (float) Math.sqrt(norm);
            for (int i = 0; i < dimensions; i++) vector[i] /= norm;
        }
        return vector;
    }

    private String computeHash(String text) {
        return UUID.nameUUIDFromBytes(text.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
