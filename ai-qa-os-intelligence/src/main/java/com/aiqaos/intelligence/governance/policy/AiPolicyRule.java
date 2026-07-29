package com.aiqaos.intelligence.governance.policy;

import com.aiqaos.core.guardrail.GuardrailContext;
import java.util.regex.Pattern;

/**
 * GOV-3: one declarative Responsible-AI rule — a compiled {@link Pattern} that, when found in AI
 * content (optionally only in a given {@link GuardrailContext.Phase}), yields {@link #getEffect()}.
 * A rule with a {@code null} pattern is inert (never matches) — used when a rule is configured but
 * empty, e.g. {@code no-production-urls} with no production hosts listed.
 */
public final class AiPolicyRule {

    private final String id;
    private final String description;
    private final PolicyEffect effect;
    private final GuardrailContext.Phase phase; // null = applies to any phase
    private final Pattern pattern; // null = inert

    public AiPolicyRule(String id, String description, PolicyEffect effect,
                        GuardrailContext.Phase phase, Pattern pattern) {
        this.id = id;
        this.description = description;
        this.effect = effect;
        this.phase = phase;
        this.pattern = pattern;
    }

    /** True if this rule applies to the given context and its pattern is present in the content. */
    public boolean matches(String content, GuardrailContext context) {
        if (pattern == null || content == null || content.isEmpty()) {
            return false;
        }
        if (phase != null && context != null && context.getPhase() != phase) {
            return false;
        }
        return pattern.matcher(content).find();
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public PolicyEffect getEffect() { return effect; }
    public GuardrailContext.Phase getPhase() { return phase; }
    public boolean isInert() { return pattern == null; }
}
