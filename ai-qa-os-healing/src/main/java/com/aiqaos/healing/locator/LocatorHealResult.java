package com.aiqaos.healing.locator;

import com.aiqaos.healing.approval.HealingApprovalDecision;

/**
 * HEAL-1 × HEAL-2: the outcome of coordinating a locator heal — the chosen candidate (nullable when
 * none could be proposed) and the approval {@link HealingApprovalDecision} that governs whether it may
 * be applied.
 */
public final class LocatorHealResult {

    private final LocatorCandidate chosen;
    private final HealingApprovalDecision decision;

    public LocatorHealResult(LocatorCandidate chosen, HealingApprovalDecision decision) {
        this.chosen = chosen;
        this.decision = decision;
    }

    public static LocatorHealResult noCandidate() {
        return new LocatorHealResult(null, null);
    }

    public LocatorCandidate getChosen() { return chosen; }
    public HealingApprovalDecision getDecision() { return decision; }

    public boolean isAutoApproved() {
        return decision != null && decision.isAutoApproved();
    }

    public boolean isPending() {
        return decision != null && decision.isPending();
    }
}
