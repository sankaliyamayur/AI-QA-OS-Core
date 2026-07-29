package com.aiqaos.healing.approval;

import com.aiqaos.core.contract.ConfidenceVerdict;

/**
 * HEAL-2: the immediate outcome of tiering a heal — its {@link HealingApprovalStatus}, a reason, and
 * the underlying {@link ConfidenceVerdict} (nullable when no gate was present). {@code AUTO_APPROVED}
 * may be applied immediately; {@code PENDING_APPROVAL} awaits a human; {@code REJECTED} must not be
 * applied.
 */
public final class HealingApprovalDecision {

    private final String healingId;
    private final HealingApprovalStatus status;
    private final String reason;
    private final ConfidenceVerdict confidenceVerdict;

    public HealingApprovalDecision(String healingId, HealingApprovalStatus status, String reason,
                                   ConfidenceVerdict confidenceVerdict) {
        this.healingId = healingId;
        this.status = status;
        this.reason = reason;
        this.confidenceVerdict = confidenceVerdict;
    }

    public boolean isAutoApproved() { return status == HealingApprovalStatus.AUTO_APPROVED; }
    public boolean isPending() { return status == HealingApprovalStatus.PENDING_APPROVAL; }

    public String getHealingId() { return healingId; }
    public HealingApprovalStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public ConfidenceVerdict getConfidenceVerdict() { return confidenceVerdict; }

    @Override
    public String toString() {
        return "HealingApprovalDecision{" + healingId + " → " + status + " (" + reason + ")}";
    }
}
