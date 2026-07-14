package com.aiqaos.learning.memory;

import com.aiqaos.core.model.FailurePattern;
import com.aiqaos.core.model.SelfHealingRecommendation;
import com.aiqaos.memory.store.MemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LearningMemoryStoreTest {

    private LearningMemoryStore store;
    private StubMemoryStore mockMemoryStore;

    @BeforeEach
    void setUp() {
        store = new LearningMemoryStore();
        mockMemoryStore = new StubMemoryStore();
        ReflectionTestUtils.setField(store, "memoryStore", mockMemoryStore);
    }

    @Test
    void testStoreAndRetrieveFailurePatterns() {
        FailurePattern pattern = new FailurePattern();
        pattern.setPatternId("PAT-1");
        pattern.setErrorType("TIMEOUT");
        pattern.setRootCause("Network lag");
        pattern.setOccurrenceCount(1);
        pattern.setConfidence(0.9);

        store.storeFailurePatterns(List.of(pattern));

        List<FailurePattern> retrieved = store.getFailurePatterns();
        assertEquals(1, retrieved.size());
        assertEquals("TIMEOUT", retrieved.get(0).getErrorType());
        assertEquals("Network lag", retrieved.get(0).getRootCause());
        assertEquals(1, retrieved.get(0).getOccurrenceCount());

        // Increment count trace
        FailurePattern pattern2 = new FailurePattern();
        pattern2.setErrorType("TIMEOUT");
        pattern2.setRootCause("Network lag");
        pattern2.setOccurrenceCount(1);
        pattern2.setConfidence(0.9);

        store.storeFailurePatterns(List.of(pattern2));
        retrieved = store.getFailurePatterns();
        assertEquals(1, retrieved.size());
        assertEquals(2, retrieved.get(0).getOccurrenceCount());
    }

    @Test
    void testStoreAndRetrieveRecommendations() {
        SelfHealingRecommendation rec = new SelfHealingRecommendation();
        rec.setRecommendationId("REC-1");
        rec.setIssue("Locator failed");
        rec.setSuggestedAction("Update path to #btn");

        store.storeRecommendations(List.of(rec));

        List<SelfHealingRecommendation> retrieved = store.getRecommendations();
        assertEquals(1, retrieved.size());
        assertEquals("REC-1", retrieved.get(0).getRecommendationId());
        assertEquals("Locator failed", retrieved.get(0).getIssue());
    }

    private static class StubMemoryStore implements MemoryStore {
        private final java.util.Map<String, Object> db = new java.util.HashMap<>();

        @Override
        public void put(String key, Object value, Duration ttl) {
            db.put(key, value);
        }

        @Override
        public Optional<Object> get(String key) {
            return Optional.ofNullable(db.get(key));
        }

        @Override
        public void remove(String key) {
            db.remove(key);
        }

        @Override
        public void clear() {
            db.clear();
        }
    }
}
