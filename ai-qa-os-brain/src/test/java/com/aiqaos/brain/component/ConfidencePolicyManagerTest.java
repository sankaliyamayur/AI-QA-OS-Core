package com.aiqaos.brain.component;

import com.aiqaos.brain.repository.DecisionRepository;
import com.aiqaos.core.contract.ConfidenceDecisionContext;
import com.aiqaos.core.contract.ConfidenceVerdict;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AI-1 — verifies the confidence-gate verdict matrix and the 0.0-ungated safeguard.
 * Thresholds high=0.90, medium=0.70. No repository (audit skipped) — avoids Mockito on JDK 25.
 */
class ConfidencePolicyManagerTest {

    private ConfidenceVerdict evaluate(double confidence) {
        ConfidencePolicyManager gate = new ConfidencePolicyManager(noRepository(), 0.90, 0.70);
        return gate.evaluate(new ConfidenceDecisionContext("TestStep", confidence, "corr-1"));
    }

    @Test
    void unreportedConfidenceIsUngated() {
        assertEquals(ConfidenceVerdict.UNGATED, evaluate(0.0));
        assertEquals(ConfidenceVerdict.UNGATED, evaluate(-1.0));
    }

    @Test
    void highConfidenceProceeds() {
        assertEquals(ConfidenceVerdict.PROCEED, evaluate(0.95));
        assertEquals(ConfidenceVerdict.PROCEED, evaluate(0.90)); // boundary inclusive
    }

    @Test
    void mediumConfidenceProceedsWithValidation() {
        assertEquals(ConfidenceVerdict.PROCEED_WITH_VALIDATION, evaluate(0.80));
        assertEquals(ConfidenceVerdict.PROCEED_WITH_VALIDATION, evaluate(0.70)); // boundary inclusive
    }

    @Test
    void lowConfidenceRequiresHumanReview() {
        assertEquals(ConfidenceVerdict.HUMAN_REVIEW, evaluate(0.69));
        assertEquals(ConfidenceVerdict.HUMAN_REVIEW, evaluate(0.01));
    }

    /** A no-op ObjectProvider that resolves no DecisionRepository (avoids Mockito on JDK 25). */
    private ObjectProvider<DecisionRepository> noRepository() {
        return new ObjectProvider<>() {
            @Override public DecisionRepository getObject(Object... args) { return null; }
            @Override public DecisionRepository getObject() { return null; }
            @Override public DecisionRepository getIfAvailable() { return null; }
            @Override public DecisionRepository getIfUnique() { return null; }
        };
    }
}
