package com.aiqaos.intelligence.governance.policy;

/**
 * GOV-3: what a matched Responsible-AI policy rule dictates, ordered by severity so the engine can
 * pick the most severe outcome when several rules match. {@link #REQUIRE_REVIEW} means "a human must
 * approve this"; until the AI-1/AI-2 gate escalation is wired (FI-GOV3-B) the guardrail enforces it
 * as a fail-safe block, so it ranks just below an outright {@link #BLOCK}.
 */
public enum PolicyEffect {
    ALLOW(0),
    WARN(1),
    REQUIRE_REVIEW(2),
    BLOCK(3);

    private final int severity;

    PolicyEffect(int severity) {
        this.severity = severity;
    }

    public int severity() {
        return severity;
    }

    /** The more severe of two effects (ties return {@code this}). */
    public PolicyEffect max(PolicyEffect other) {
        return other != null && other.severity > this.severity ? other : this;
    }
}
