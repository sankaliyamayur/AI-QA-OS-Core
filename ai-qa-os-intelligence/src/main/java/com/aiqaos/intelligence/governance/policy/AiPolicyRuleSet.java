package com.aiqaos.intelligence.governance.policy;

import com.aiqaos.core.guardrail.GuardrailContext;
import java.util.ArrayList;
import java.util.List;

/**
 * GOV-3: an ordered set of {@link AiPolicyRule}s evaluated together. If several rules match, the
 * <em>most severe</em> effect wins (a PII {@code BLOCK} outranks a destructive {@code REQUIRE_REVIEW}),
 * so the decision is deterministic regardless of rule order. No match → {@link PolicyDecision#allow()}.
 */
public final class AiPolicyRuleSet {

    private final List<AiPolicyRule> rules;

    public AiPolicyRuleSet(List<AiPolicyRule> rules) {
        this.rules = rules == null ? new ArrayList<>() : new ArrayList<>(rules);
    }

    public PolicyDecision evaluate(String content, GuardrailContext context) {
        AiPolicyRule winner = null;
        for (AiPolicyRule rule : rules) {
            if (rule.matches(content, context)) {
                if (winner == null || rule.getEffect().severity() > winner.getEffect().severity()) {
                    winner = rule;
                }
            }
        }
        if (winner == null) {
            return PolicyDecision.allow();
        }
        return PolicyDecision.of(winner.getEffect(), winner.getId(), winner.getDescription());
    }

    public List<AiPolicyRule> getRules() {
        return new ArrayList<>(rules);
    }
}
