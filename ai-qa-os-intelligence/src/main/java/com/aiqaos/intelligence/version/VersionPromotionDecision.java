package com.aiqaos.intelligence.version;

import com.aiqaos.intelligence.governance.policy.PolicyDecision;

/**
 * GOV-3 × GOV-4: the outcome of a policy-gated version promotion — whether the version was
 * {@code PROMOTED} (and the resulting {@link VersionPin}) or {@code BLOCKED} by the Responsible-AI
 * policy (with the offending {@link PolicyDecision}).
 */
public final class VersionPromotionDecision {

    public enum PromotionStatus { PROMOTED, BLOCKED }

    private final PromotionStatus status;
    private final String reason;
    private final VersionPin pin;              // present when PROMOTED
    private final PolicyDecision policyDecision; // present when BLOCKED

    private VersionPromotionDecision(PromotionStatus status, String reason, VersionPin pin,
                                     PolicyDecision policyDecision) {
        this.status = status;
        this.reason = reason;
        this.pin = pin;
        this.policyDecision = policyDecision;
    }

    public static VersionPromotionDecision promoted(VersionPin pin) {
        return new VersionPromotionDecision(PromotionStatus.PROMOTED, "policy cleared; version pinned",
                pin, null);
    }

    public static VersionPromotionDecision blocked(String reason, PolicyDecision policyDecision) {
        return new VersionPromotionDecision(PromotionStatus.BLOCKED, reason, null, policyDecision);
    }

    public boolean isPromoted() { return status == PromotionStatus.PROMOTED; }

    public PromotionStatus getStatus() { return status; }
    public String getReason() { return reason; }
    public VersionPin getPin() { return pin; }
    public PolicyDecision getPolicyDecision() { return policyDecision; }

    @Override
    public String toString() {
        return "VersionPromotionDecision{" + status + ": " + reason + "}";
    }
}
