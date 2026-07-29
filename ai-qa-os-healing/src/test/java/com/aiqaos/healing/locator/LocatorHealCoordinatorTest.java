package com.aiqaos.healing.locator;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.contract.ConfidenceGate;
import com.aiqaos.core.contract.ConfidenceVerdict;
import com.aiqaos.healing.approval.HealingApprovalProperties;
import com.aiqaos.healing.approval.HealingApprovalService;
import com.aiqaos.healing.approval.HealingApprovalStatus;
import com.aiqaos.healing.approval.InMemoryHealingApprovalStore;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * HEAL-1 × HEAL-2: unit tests for the coordinator — a strong locator auto-approves, a brittle one is
 * routed to approval, and an unfindable one yields no candidate. No Mockito.
 */
class LocatorHealCoordinatorTest {

    private final HeuristicLocatorHealer healer = new HeuristicLocatorHealer();

    private final ConfidenceGate gateStub = ctx -> {
        double c = ctx.getConfidence();
        if (c <= 0.0) return ConfidenceVerdict.UNGATED;
        if (c >= 0.90) return ConfidenceVerdict.PROCEED;
        if (c >= 0.70) return ConfidenceVerdict.PROCEED_WITH_VALIDATION;
        return ConfidenceVerdict.HUMAN_REVIEW;
    };

    private LocatorHealCoordinator coordinator() {
        HealingApprovalService approval = new HealingApprovalService(
                new InMemoryHealingApprovalStore(), new HealingApprovalProperties(), gateStub);
        return new LocatorHealCoordinator(healer, approval);
    }

    @Test
    void strongLocatorAutoApproves() {
        LocatorHealResult r = coordinator().heal(
                LocatorHealingRequest.of("#old", Map.of("data-testid", "submit"))); // conf 0.95 → PROCEED
        assertThat(r.isAutoApproved()).isTrue();
        assertThat(r.getChosen().getStrategy()).isEqualTo(LocatorStrategy.TEST_ID);
        assertThat(r.getDecision().getStatus()).isEqualTo(HealingApprovalStatus.AUTO_APPROVED);
    }

    @Test
    void brittleLocatorIsRoutedToApproval() {
        LocatorHealResult r = coordinator().heal(
                LocatorHealingRequest.of("//div[2]/button[3]", Map.of())); // conf 0.35 → HUMAN_REVIEW
        assertThat(r.isAutoApproved()).isFalse();
        assertThat(r.isPending()).isTrue();
        assertThat(r.getDecision().getStatus()).isEqualTo(HealingApprovalStatus.PENDING_APPROVAL);
    }

    @Test
    void noCandidateYieldsNoResult() {
        LocatorHealResult r = coordinator().heal(LocatorHealingRequest.of(null, Map.of()));
        assertThat(r.getChosen()).isNull();
        assertThat(r.getDecision()).isNull();
    }
}
