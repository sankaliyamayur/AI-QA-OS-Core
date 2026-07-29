package com.aiqaos.intelligence.governance.policy;

/**
 * GOV-3: the outcome of evaluating AI content against the Responsible-AI rule set — the effect to
 * apply, the rule that produced it, and a human-readable reason. {@link #allow()} is the default
 * when no rule matches.
 */
public final class PolicyDecision {

    private final PolicyEffect effect;
    private final String ruleId;
    private final String reason;

    private PolicyDecision(PolicyEffect effect, String ruleId, String reason) {
        this.effect = effect;
        this.ruleId = ruleId;
        this.reason = reason;
    }

    /** No rule matched — the content is permitted. */
    public static PolicyDecision allow() {
        return new PolicyDecision(PolicyEffect.ALLOW, null, "no policy rule matched");
    }

    /** A rule matched; apply {@code effect} and record which rule and why. */
    public static PolicyDecision of(PolicyEffect effect, String ruleId, String reason) {
        return new PolicyDecision(effect, ruleId, reason);
    }

    public PolicyEffect getEffect() { return effect; }
    public String getRuleId() { return ruleId; }
    public String getReason() { return reason; }

    /** Whether this decision permits the content ({@code ALLOW}/{@code WARN}). */
    public boolean isPermitted() {
        return effect == PolicyEffect.ALLOW || effect == PolicyEffect.WARN;
    }

    @Override
    public String toString() {
        return "PolicyDecision{" + effect + (ruleId != null ? " by " + ruleId : "") + ": " + reason + "}";
    }
}
