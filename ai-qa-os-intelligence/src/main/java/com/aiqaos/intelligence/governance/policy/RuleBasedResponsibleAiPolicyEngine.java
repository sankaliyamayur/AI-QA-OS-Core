package com.aiqaos.intelligence.governance.policy;

import com.aiqaos.core.guardrail.GuardrailContext;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * GOV-3: deterministic, config-driven {@link ResponsibleAiPolicyEngine}. Builds an
 * {@link AiPolicyRuleSet} from {@link ResponsibleAiPolicyProperties} at construction — a
 * production-host block, a PII block, a destructive-action review flag, plus any operator-supplied
 * block patterns — and evaluates content against it (most-severe match wins). No I/O, no live model:
 * fully unit-testable. A live OPA/Rego engine is a drop-in alternative behind the seam (FI-GOV3-A).
 */
@Component
public class RuleBasedResponsibleAiPolicyEngine implements ResponsibleAiPolicyEngine {

    private final AiPolicyRuleSet ruleSet;

    public RuleBasedResponsibleAiPolicyEngine(ResponsibleAiPolicyProperties properties) {
        this.ruleSet = buildRuleSet(properties);
    }

    @Override
    public PolicyDecision evaluate(String content, GuardrailContext context) {
        return ruleSet.evaluate(content, context);
    }

    private static AiPolicyRuleSet buildRuleSet(ResponsibleAiPolicyProperties props) {
        List<AiPolicyRule> rules = new ArrayList<>();

        // no-production-urls: hosts that must never appear in AI content. Inert until configured.
        Pattern hostPattern = productionHostPattern(props.getProductionHosts());
        rules.add(new AiPolicyRule("no-production-urls",
                "AI content references a production host", PolicyEffect.BLOCK, null, hostPattern));

        // no-pii-in-prompts: emails, SSN-shaped, and card-shaped numbers.
        if (props.isBlockPii()) {
            Pattern pii = Pattern.compile(
                    "(?i)([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
                            + "|\\b\\d{3}-\\d{2}-\\d{4}\\b"
                            + "|\\b\\d{13,16}\\b)");
            rules.add(new AiPolicyRule("no-pii-in-prompts",
                    "AI content contains PII (email / SSN / card number)", PolicyEffect.BLOCK, null, pii));
        }

        // destructive-requires-review: destructive-action keywords → human review (fail-safe block).
        if (props.isReviewDestructive()) {
            Pattern destructive = Pattern.compile(
                    "(?i)\\b(drop\\s+table|drop\\s+database|delete\\s+from|truncate\\s+table"
                            + "|truncate\\b|rm\\s+-rf)\\b");
            rules.add(new AiPolicyRule("destructive-requires-review",
                    "AI content requests a destructive action", PolicyEffect.REQUIRE_REVIEW, null, destructive));
        }

        // Operator-supplied extra block patterns.
        int i = 0;
        for (String regex : props.getBlockPatterns()) {
            if (regex != null && !regex.isBlank()) {
                rules.add(new AiPolicyRule("custom-block-" + i++,
                        "AI content matched a custom block pattern", PolicyEffect.BLOCK, null,
                        Pattern.compile(regex)));
            }
        }

        return new AiPolicyRuleSet(rules);
    }

    /** Build a literal-alternation pattern over the configured hosts, or {@code null} if none. */
    private static Pattern productionHostPattern(List<String> hosts) {
        List<String> quoted = new ArrayList<>();
        if (hosts != null) {
            for (String host : hosts) {
                if (host != null && !host.isBlank()) {
                    quoted.add(Pattern.quote(host.trim()));
                }
            }
        }
        if (quoted.isEmpty()) {
            return null; // inert — no production hosts configured
        }
        return Pattern.compile("(?i)(" + String.join("|", quoted) + ")");
    }
}
