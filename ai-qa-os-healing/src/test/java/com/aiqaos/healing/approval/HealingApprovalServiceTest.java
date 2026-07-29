package com.aiqaos.healing.approval;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.contract.ConfidenceGate;
import com.aiqaos.core.contract.ConfidenceVerdict;
import org.junit.jupiter.api.Test;

/**
 * HEAL-2: unit tests for the strict confidence tier + approval lifecycle. ConfidenceGate stub mirrors
 * AI-1 thresholds (high 0.90 / medium 0.70). No Mockito.
 */
class HealingApprovalServiceTest {

    private final HealingApprovalProperties props = new HealingApprovalProperties(); // auto .90 / floor .50

    private final ConfidenceGate gateStub = ctx -> {
        double c = ctx.getConfidence();
        if (c <= 0.0) return ConfidenceVerdict.UNGATED;
        if (c >= 0.90) return ConfidenceVerdict.PROCEED;
        if (c >= 0.70) return ConfidenceVerdict.PROCEED_WITH_VALIDATION;
        return ConfidenceVerdict.HUMAN_REVIEW;
    };

    private HealingApprovalService gated() {
        return new HealingApprovalService(new InMemoryHealingApprovalStore(), props, gateStub);
    }

    @Test
    void fullProceedAutoApproves() {
        HealingApprovalDecision d = gated().decide("H1", "swap locator", 0.95, "c1");
        assertThat(d.getStatus()).isEqualTo(HealingApprovalStatus.AUTO_APPROVED);
        assertThat(d.getConfidenceVerdict()).isEqualTo(ConfidenceVerdict.PROCEED);
    }

    @Test
    void proceedWithValidationRequiresApproval_strict() {
        HealingApprovalService svc = gated();
        HealingApprovalDecision d = svc.decide("H2", "swap locator", 0.75, "c1"); // medium
        assertThat(d.getStatus()).isEqualTo(HealingApprovalStatus.PENDING_APPROVAL);
        assertThat(svc.pending()).extracting(HealingApprovalRequest::getHealingId).containsExactly("H2");
    }

    @Test
    void humanReviewIsPending() {
        HealingApprovalDecision d = gated().decide("H3", "swap locator", 0.60, "c1");
        assertThat(d.getStatus()).isEqualTo(HealingApprovalStatus.PENDING_APPROVAL);
    }

    @Test
    void unreportedConfidenceIsRejectedFailSafe() {
        HealingApprovalDecision d = gated().decide("H4", "swap locator", 0.0, "c1");
        assertThat(d.getStatus()).isEqualTo(HealingApprovalStatus.REJECTED);
        assertThat(d.getConfidenceVerdict()).isEqualTo(ConfidenceVerdict.UNGATED);
    }

    @Test
    void gateAbsentUsesLocalThresholds() {
        HealingApprovalService svc = new HealingApprovalService(new InMemoryHealingApprovalStore(), props);
        assertThat(svc.decide("A", "x", 0.95, null).getStatus()).isEqualTo(HealingApprovalStatus.AUTO_APPROVED);
        assertThat(svc.decide("B", "x", 0.60, null).getStatus()).isEqualTo(HealingApprovalStatus.PENDING_APPROVAL);
        assertThat(svc.decide("C", "x", 0.30, null).getStatus()).isEqualTo(HealingApprovalStatus.REJECTED);
    }

    @Test
    void approveTransitionsPendingToApprovedAndClearsFromPending() {
        HealingApprovalService svc = gated();
        svc.decide("H5", "swap locator", 0.75, "c1"); // pending
        assertThat(svc.approve("H5")).isTrue();
        assertThat(svc.pending()).isEmpty();
    }

    @Test
    void rejectTransitionsPendingToRejected() {
        HealingApprovalService svc = gated();
        svc.decide("H6", "swap locator", 0.75, "c1");
        assertThat(svc.reject("H6")).isTrue();
        assertThat(svc.pending()).isEmpty();
    }

    @Test
    void resolvingUnknownOrNonPendingReturnsFalse() {
        HealingApprovalService svc = gated();
        assertThat(svc.approve("nope")).isFalse();       // unknown
        svc.decide("H7", "x", 0.95, "c1");               // AUTO_APPROVED, not pending
        assertThat(svc.approve("H7")).isFalse();          // not pending → cannot approve
    }
}
