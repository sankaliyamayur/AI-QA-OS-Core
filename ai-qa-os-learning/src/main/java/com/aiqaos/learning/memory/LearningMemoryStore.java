package com.aiqaos.learning.memory;

import com.aiqaos.core.model.FailurePattern;
import com.aiqaos.core.model.SelfHealingRecommendation;
import com.aiqaos.learning.reflection.ImprovementProposal;
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
    private static final String IMPROVEMENT_PROPOSALS_KEY = "learning:improvement_proposals";
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

    /**
     * LRN-1: record improvement proposals from the reflection stage, keyed by {@code proposalId}
     * (a re-recorded proposal replaces the prior one). Recorded, not adopted — adoption is gated on
     * LRN-4.
     */
    public void storeImprovementProposals(List<ImprovementProposal> proposals) {
        if (proposals == null || proposals.isEmpty()) {
            return;
        }
        List<ImprovementProposal> existing = getImprovementProposals();
        for (ImprovementProposal p : proposals) {
            existing.removeIf(e -> e.getProposalId() != null
                    && e.getProposalId().equals(p.getProposalId()));
            existing.add(p);
        }
        memoryStore.put(IMPROVEMENT_PROPOSALS_KEY, existing, TTL);
    }

    public List<ImprovementProposal> getImprovementProposals() {
        Optional<Object> val = memoryStore.get(IMPROVEMENT_PROPOSALS_KEY);
        if (val.isPresent() && val.get() instanceof List) {
            return new ArrayList<>((List<ImprovementProposal>) val.get());
        }
        return new ArrayList<>();
    }

    public void clear() {
        memoryStore.remove(FAILURE_PATTERNS_KEY);
        memoryStore.remove(RECOMMENDATIONS_KEY);
        memoryStore.remove(IMPROVEMENT_PROPOSALS_KEY);
    }
}
