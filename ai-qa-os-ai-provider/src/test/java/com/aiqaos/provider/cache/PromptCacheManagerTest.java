package com.aiqaos.provider.cache;

import com.aiqaos.memory.ranking.SimilarityCalculator;
import com.aiqaos.memory.vector.InMemoryVectorStoreClient;
import com.aiqaos.memory.vector.VectorStoreClient;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PromptCacheManagerTest {

    private PromptCacheProperties properties;
    private VectorStoreClient vectorStoreClient;
    private LlmSemanticCacheManager cacheManager;

    @BeforeEach
    void setUp() {
        properties = new PromptCacheProperties();
        properties.setEnabled(true);
        properties.setSimilarityThreshold(0.90);
        properties.setCollection("prompt_cache");

        vectorStoreClient = new InMemoryVectorStoreClient(new SimilarityCalculator());
        cacheManager = new LlmSemanticCacheManager(properties, vectorStoreClient);
    }

    @Test
    void testCacheMissAndHit() {
        LLMRequest request = new LLMRequest();
        request.setPrompt("Generate test cases for login endpoint");
        request.setPurpose("test-generation");

        // Initial lookup -> Miss
        Optional<LLMResponse> miss = cacheManager.findCachedResponse(request);
        assertTrue(miss.isEmpty());

        // Cache a response
        LLMResponse response = new LLMResponse("Test case 1: valid login", "gemini-1.5-flash", new TokenUsage(100, 50), 1200L);
        cacheManager.cacheResponse(request, response);

        // Identical prompt lookup -> Hit
        Optional<LLMResponse> hit = cacheManager.findCachedResponse(request);
        assertTrue(hit.isPresent());
        assertEquals("Test case 1: valid login", hit.get().getText());
        assertEquals("prompt-cache", hit.get().getModel());
        assertEquals(0, hit.get().getUsage().getInputTokens());
        assertEquals(1L, hit.get().getLatencyMs());
    }

    @Test
    void testDisabledCache() {
        properties.setEnabled(false);

        LLMRequest request = new LLMRequest();
        request.setPrompt("Some prompt text");

        LLMResponse response = new LLMResponse("Output text", "gemini-1.5-flash", new TokenUsage(10, 10), 100L);
        cacheManager.cacheResponse(request, response);

        Optional<LLMResponse> result = cacheManager.findCachedResponse(request);
        assertTrue(result.isEmpty());
    }
}
