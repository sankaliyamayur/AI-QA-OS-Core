package com.aiqaos.brain.component;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.contract.AdoptionCandidate;
import com.aiqaos.core.contract.AdoptionDecision;
import com.aiqaos.core.contract.AdoptionKind;
import com.aiqaos.core.contract.ConfidenceGate;
import com.aiqaos.core.contract.ConfidenceVerdict;
import org.junit.jupiter.api.Test;

/**
 * LRN-4: unit tests for the safe-adoption gate — admit only when eval passes AND confidence clears;
 * every other outcome (incl. unreported confidence) is rejected for review. Uses a ConfidenceGate
 * stub mirroring AI-1 thresholds (high 0.90 / medium 0.70); no Mockito.
 */
class LearningAdoptionGateTest {

    private static final double EVAL_THRESHOLD = 0.75;

    /** Stub of the AI-1 gate: maps a confidence value to a verdict like ConfidencePolicyManager. */
    private final ConfidenceGate confidenceStub = ctx -> {
        double c = ctx.getConfidence();
        if (c <= 0.0) return ConfidenceVerdict.UNGATED;
        if (c >= 0.90) return ConfidenceVerdict.PROCEED;
        if (c >= 0.70) return ConfidenceVerdict.PROCEED_WITH_VALIDATION;
        return ConfidenceVerdict.HUMAN_REVIEW;
    };

    private final LearningAdoptionGate gate = new LearningAdoptionGate(confidenceStub, EVAL_THRESHOLD);

    private AdoptionCandidate candidate(double evalScore, double confidence) {
        return new AdoptionCandidate("CAND-1", AdoptionKind.PROMPT, evalScore, confidence,
                "improve login prompt", "corr-1");
    }

    @Test
    void admitsWhenEvalPassesAndConfidenceProceeds() {
        AdoptionDecision d = gate.evaluate(candidate(0.90, 0.95));
        assertThat(d.isAdmitted()).isTrue();
        assertThat(d.getCandidateId()).isEqualTo("CAND-1");
        assertThat(d.getConfidenceVerdict()).isEqualTo(ConfidenceVerdict.PROCEED);
    }

    @Test
    void admitsWhenConfidenceIsProceedWithValidation() {
        AdoptionDecision d = gate.evaluate(candidate(0.80, 0.75)); // medium confidence still proceeds
        assertThat(d.isAdmitted()).isTrue();
        assertThat(d.getConfidenceVerdict()).isEqualTo(ConfidenceVerdict.PROCEED_WITH_VALIDATION);
    }

    @Test
    void rejectsWhenEvalBelowThreshold() {
        AdoptionDecision d = gate.evaluate(candidate(0.50, 0.95)); // eval fails despite high confidence
        assertThat(d.isAdmitted()).isFalse();
        assertThat(d.getReason()).contains("eval");
    }

    @Test
    void rejectsWhenConfidenceRoutesToHumanReview() {
        AdoptionDecision d = gate.evaluate(candidate(0.90, 0.50)); // low confidence
        assertThat(d.isAdmitted()).isFalse();
        assertThat(d.getConfidenceVerdict()).isEqualTo(ConfidenceVerdict.HUMAN_REVIEW);
        assertThat(d.getReason()).contains("HUMAN_REVIEW");
    }

    @Test
    void rejectsUnreportedConfidenceFailSafe() {
        // c <= 0 → UNGATED → rejected (opposite of AI-1's pipeline safeguard).
        AdoptionDecision d = gate.evaluate(candidate(0.95, 0.0));
        assertThat(d.isAdmitted()).isFalse();
        assertThat(d.getConfidenceVerdict()).isEqualTo(ConfidenceVerdict.UNGATED);
        assertThat(d.getReason()).contains("not reported");
    }

    @Test
    void evalThresholdIsInclusive() {
        AdoptionDecision d = gate.evaluate(candidate(0.75, 0.95)); // exactly at threshold
        assertThat(d.isAdmitted()).isTrue();
    }

    @Test
    void reasonNamesBothFailedDimensions() {
        AdoptionDecision d = gate.evaluate(candidate(0.40, 0.40)); // eval + confidence both fail
        assertThat(d.getReason()).contains("eval").contains("HUMAN_REVIEW");
    }

    @Test
    void nullCandidateIsRejected() {
        assertThat(gate.evaluate(null).isAdmitted()).isFalse();
    }
}
