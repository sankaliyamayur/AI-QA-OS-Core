package com.aiqaos.learning.memory;

import com.aiqaos.core.model.FailurePattern;
import com.aiqaos.core.model.SelfHealingRecommendation;
import com.aiqaos.memory.store.MemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@SuppressWarnings("unchecked")
public class LearningMemoryStore {

    @Autowired
    private MemoryStore memoryStore;

    private static final String FAILURE_PATTERNS_KEY = "learning:failure_patterns";
    private static final String RECOMMENDATIONS_KEY = "learning:recommendations";
    private static final Duration TTL = Duration.ofDays(30);

    public void storeFailurePatterns(List<FailurePattern> patterns) {
        List<FailurePattern> existing = getFailurePatterns();
        for (FailurePattern p : patterns) {
            // merge or add
            Optional<FailurePattern> match = existing.stream()
                    .filter(e -> e.getErrorType().equals(p.getErrorType()) && e.getRootCause().equals(p.getRootCause()))
                    .findFirst();
            if (match.isPresent()) {
                FailurePattern m = match.get();
                m.setOccurrenceCount(m.getOccurrenceCount() + p.getOccurrenceCount());
                m.setConfidence(Math.max(m.getConfidence(), p.getConfidence()));
                m.setLastDetected(p.getLastDetected());
            } else {
                existing.add(p);
            }
        }
        memoryStore.put(FAILURE_PATTERNS_KEY, existing, TTL);
    }

    public List<FailurePattern> getFailurePatterns() {
        Optional<Object> val = memoryStore.get(FAILURE_PATTERNS_KEY);
        if (val.isPresent() && val.get() instanceof List) {
            return new ArrayList<>((List<FailurePattern>) val.get());
        }
        return new ArrayList<>();
    }

    public void storeRecommendations(List<SelfHealingRecommendation> recommendations) {
        List<SelfHealingRecommendation> existing = getRecommendations();
        for (SelfHealingRecommendation r : recommendations) {
            // add or update if recommendationId matches
            existing.removeIf(e -> e.getRecommendationId().equals(r.getRecommendationId()));
            existing.add(r);
        }
        memoryStore.put(RECOMMENDATIONS_KEY, existing, TTL);
    }

    public List<SelfHealingRecommendation> getRecommendations() {
        Optional<Object> val = memoryStore.get(RECOMMENDATIONS_KEY);
        if (val.isPresent() && val.get() instanceof List) {
            return new ArrayList<>((List<SelfHealingRecommendation>) val.get());
        }
        return new ArrayList<>();
    }

    public void clear() {
        memoryStore.remove(FAILURE_PATTERNS_KEY);
        memoryStore.remove(RECOMMENDATIONS_KEY);
    }
}
