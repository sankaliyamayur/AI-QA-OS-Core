package com.aiqaos.memory.embedding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BatchEmbeddingServiceTest {

    private BatchEmbeddingService batchEmbeddingService;

    @BeforeEach
    void setUp() {
        // Use real EmbeddingCacheManager (no Spring context needed)
        batchEmbeddingService = new BatchEmbeddingService(new EmbeddingCacheManager());
    }

    @Test
    void testBatchEmbedReturnsSameCountAsInput() {
        List<String> texts = List.of("login page test", "checkout flow", "payment gateway");
        List<float[]> embeddings = batchEmbeddingService.embedBatch(texts);

        assertEquals(3, embeddings.size());
        for (float[] emb : embeddings) {
            assertEquals(128, emb.length);
        }
    }

    @Test
    void testDifferentTextsProduceDifferentEmbeddings() {
        List<String> texts = List.of("login page", "logout page");
        List<float[]> embeddings = batchEmbeddingService.embedBatch(texts);

        // Embeddings for different texts should differ
        assertFalse(arraysEqual(embeddings.get(0), embeddings.get(1)),
                "Different texts must produce different embeddings");
    }

    @Test
    void testSameTextProducesSameEmbedding() {
        float[] e1 = batchEmbeddingService.computeEmbedding("same prompt");
        float[] e2 = batchEmbeddingService.computeEmbedding("same prompt");

        assertArrayEquals(e1, e2, 0.0001f, "Identical texts must produce identical embeddings");
    }

    @Test
    void testDedupedBatchDeduplicatesDuplicateTexts() {
        // "login" appears twice — should only be embedded once
        List<String> texts = List.of("login page", "checkout", "login page");
        Map<String, float[]> result = batchEmbeddingService.embedBatchDeduped(texts);

        assertEquals(2, result.size(), "Deduped batch should have 2 unique entries");
        assertTrue(result.containsKey("login page"));
        assertTrue(result.containsKey("checkout"));
    }

    @Test
    void testEmptyBatchReturnsEmptyList() {
        List<float[]> result = batchEmbeddingService.embedBatch(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void testNullBatchReturnsEmptyList() {
        List<float[]> result = batchEmbeddingService.embedBatch(null);
        assertTrue(result.isEmpty());
    }

    private boolean arraysEqual(float[] a, float[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (Math.abs(a[i] - b[i]) > 0.0001f) return false;
        }
        return true;
    }
}
