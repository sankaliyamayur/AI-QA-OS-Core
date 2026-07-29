package com.aiqaos.healing.locator;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.contract.ConfidenceGate;
import com.aiqaos.core.contract.ConfidenceVerdict;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * HEAL-1: unit tests for confidence-gated locator selection — apply only when the gate clears (or,
 * gate-absent, the local threshold is met); otherwise surface for review. No Mockito.
 */
class LocatorHealingServiceTest {

    private final HeuristicLocatorHealer healer = new HeuristicLocatorHealer();
    private final LocatorHealingProperties props = new LocatorHealingProperties(); // min 0.70

    /** ConfidenceGate stub mirroring AI-1 thresholds (high 0.90 / medium 0.70). */
    private final ConfidenceGate gateStub = ctx -> {
        double c = ctx.getConfidence();
        if (c <= 0.0) return ConfidenceVerdict.UNGATED;
        if (c >= 0.90) return ConfidenceVerdict.PROCEED;
        if (c >= 0.70) return ConfidenceVerdict.PROCEED_WITH_VALIDATION;
        return ConfidenceVerdict.HUMAN_REVIEW;
    };

    private LocatorHealingRequest withTestId() {
        return LocatorHealingRequest.of("#old", Map.of("data-testid", "submit")); // best conf 0.95
    }

    private LocatorHealingRequest onlyBrittleXpath() {
        return LocatorHealingRequest.of("//div[2]/button[3]", Map.of()); // best conf 0.35 (XPATH)
    }

    @Test
    void appliesWhenGateProceeds() {
        LocatorHealingService svc = new LocatorHealingService(healer, props, gateStub);
        LocatorHealingOutcome o = svc.heal(withTestId());
        assertThat(o.isApplied()).isTrue();
        assertThat(o.getConfidenceVerdict()).isEqualTo(ConfidenceVerdict.PROCEED);
        assertThat(o.getChosen().getStrategy()).isEqualTo(LocatorStrategy.TEST_ID);
    }

    @Test
    void doesNotApplyWhenGateRoutesToReview() {
        LocatorHealingService svc = new LocatorHealingService(healer, props, gateStub);
        LocatorHealingOutcome o = svc.heal(onlyBrittleXpath()); // 0.35 → HUMAN_REVIEW
        assertThat(o.isApplied()).isFalse();
        assertThat(o.getConfidenceVerdict()).isEqualTo(ConfidenceVerdict.HUMAN_REVIEW);
        assertThat(o.getChosen()).isNotNull(); // still surfaced for review
    }

    @Test
    void gateAbsentUsesLocalThreshold_apply() {
        LocatorHealingService svc = new LocatorHealingService(healer, props); // no gate
        LocatorHealingOutcome o = svc.heal(withTestId()); // 0.95 ≥ 0.70
        assertThat(o.isApplied()).isTrue();
        assertThat(o.getConfidenceVerdict()).isNull();
    }

    @Test
    void gateAbsentUsesLocalThreshold_reject() {
        LocatorHealingService svc = new LocatorHealingService(healer, props); // no gate
        LocatorHealingOutcome o = svc.heal(onlyBrittleXpath()); // 0.35 < 0.70
        assertThat(o.isApplied()).isFalse();
    }

    @Test
    void noCandidateYieldsNonAppliedOutcome() {
        LocatorHealingService svc = new LocatorHealingService(healer, props, gateStub);
        LocatorHealingOutcome o = svc.heal(LocatorHealingRequest.of(null, Map.of()));
        assertThat(o.isApplied()).isFalse();
        assertThat(o.getChosen()).isNull();
        assertThat(o.getReason()).contains("no candidate");
    }
}
