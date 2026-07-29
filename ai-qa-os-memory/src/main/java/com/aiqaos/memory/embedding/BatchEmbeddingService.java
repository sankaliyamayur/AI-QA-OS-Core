package com.aiqaos.memory.embedding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PERF-2: Batch Embeddings — computes or retrieves embeddings for multiple texts
 * in a single operation to eliminate per-item round-trips and serial latency.
 *
 * Integrates with {@link EmbeddingCacheManager} to skip recomputing embeddings
 * for prompts already seen (by SHA-256 hash).
 */
@Component
public class BatchEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(BatchEmbeddingService.class);

    private static final int EMBEDDING_DIMENSIONS = 128;

    private final EmbeddingCacheManager embeddingCacheManager;

    public BatchEmbeddingService(EmbeddingCacheManager embeddingCacheManager) {
        this.embeddingCacheManager = embeddingCacheManager;
    }

    /**
     * Computes embeddings for a batch of texts.
     * Cache hits are resolved instantly; only cache misses are computed.
     *
     * @param texts  ordered list of texts to embed
     * @return ordered list of float[] embeddings (same order as input)
     */
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        List<float[]> results = new ArrayList<>(texts.size());
        int cacheHits = 0;
        int cacheMisses = 0;

        for (String text : texts) {
            if (text == null || text.isBlank()) {
                results.add(new float[EMBEDDING_DIMENSIONS]);
                continue;
            }

            String hash = embeddingCacheManager.computeHash(text);
            float[] cached = embeddingCacheManager.getCachedEmbedding(hash);

            if (cached != null) {
                results.add(cached);
                cacheHits++;
            } else {
                float[] embedding = computeEmbedding(text);
                results.add(embedding);
                cacheMisses++;
            }
        }

        log.info("PERF-2: Batch embedded {} texts — cache hits: {}, misses (computed): {}",
                texts.size(), cacheHits, cacheMisses);

        return results;
    }

    /**
     * Returns a map of text -> embedding, deduplicating identical texts
     * to avoid computing the same embedding twice in a batch.
     */
    public Map<String, float[]> embedBatchDeduped(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Map.of();
        }

        // LinkedHashMap preserves insertion order for reproducible iteration
        Map<String, float[]> resultMap = new LinkedHashMap<>();

        for (String text : texts) {
            if (text == null || text.isBlank()) continue;
            if (resultMap.containsKey(text)) continue; // deduplication — skip already computed

            String hash = embeddingCacheManager.computeHash(text);
            float[] cached = embeddingCacheManager.getCachedEmbedding(hash);

            resultMap.put(text, cached != null ? cached : computeEmbedding(text));
        }

        int dedupedCount = texts.size() - resultMap.size();
        log.debug("PERF-2: Deduped batch: {} unique from {} total inputs, {} skipped as duplicates",
                resultMap.size(), texts.size(), dedupedCount);

        return resultMap;
    }

    /**
     * Deterministic 128-dim feature embedding.
     * Matches the algorithm used in LlmSemanticCacheManager for cross-component consistency.
     */
    float[] computeEmbedding(String text) {
        float[] vector = new float[EMBEDDING_DIMENSIONS];
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            int idx = Math.abs(bytes[i] * 31 + i) % EMBEDDING_DIMENSIONS;
            vector[idx] += (bytes[i] & 0xFF) / 255.0f;
        }
        // L2-normalize
        float norm = 0.0f;
        for (float v : vector) norm += v * v;
        if (norm > 0) {
            norm = (float) Math.sqrt(norm);
            for (int i = 0; i < EMBEDDING_DIMENSIONS; i++) vector[i] /= norm;
        }
        return vector;
    }
}
