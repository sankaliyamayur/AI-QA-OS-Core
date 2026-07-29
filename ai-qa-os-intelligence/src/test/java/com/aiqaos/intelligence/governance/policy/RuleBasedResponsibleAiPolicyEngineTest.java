package com.aiqaos.intelligence.governance.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.guardrail.GuardrailContext;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * GOV-3: unit tests for the config-driven Responsible-AI rule engine (most-severe match, each
 * default rule, config overrides). No Spring, no Mockito.
 */
class RuleBasedResponsibleAiPolicyEngineTest {

    private final GuardrailContext ctx = GuardrailContext.input("test");

    private ResponsibleAiPolicyEngine engine(ResponsibleAiPolicyProperties props) {
        return new RuleBasedResponsibleAiPolicyEngine(props);
    }

    private ResponsibleAiPolicyProperties defaults() {
        return new ResponsibleAiPolicyProperties(); // enabled, enforce, PII + destructive on
    }

    @Test
    void cleanContentIsAllowed() {
        PolicyDecision d = engine(defaults()).evaluate("Navigate to the login page and verify the header", ctx);
        assertThat(d.getEffect()).isEqualTo(PolicyEffect.ALLOW);
        assertThat(d.isPermitted()).isTrue();
    }

    @Test
    void piiEmailIsBlocked() {
        PolicyDecision d = engine(defaults()).evaluate("Log in as alice@example.com and continue", ctx);
        assertThat(d.getEffect()).isEqualTo(PolicyEffect.BLOCK);
        assertThat(d.getRuleId()).isEqualTo("no-pii-in-prompts");
    }

    @Test
    void ssnShapedNumberIsBlocked() {
        PolicyDecision d = engine(defaults()).evaluate("The user's SSN is 123-45-6789", ctx);
        assertThat(d.getEffect()).isEqualTo(PolicyEffect.BLOCK);
        assertThat(d.getRuleId()).isEqualTo("no-pii-in-prompts");
    }

    @Test
    void destructiveActionRequiresReview() {
        PolicyDecision d = engine(defaults()).evaluate("Then run DROP TABLE users to reset", ctx);
        assertThat(d.getEffect()).isEqualTo(PolicyEffect.REQUIRE_REVIEW);
        assertThat(d.getRuleId()).isEqualTo("destructive-requires-review");
        assertThat(d.isPermitted()).isFalse();
    }

    @Test
    void mostSevereEffectWins_piiBlockOutranksDestructiveReview() {
        // Content trips BOTH the destructive (REQUIRE_REVIEW) and PII (BLOCK) rules.
        PolicyDecision d = engine(defaults())
                .evaluate("DELETE FROM accounts WHERE email = 'bob@example.com'", ctx);
        assertThat(d.getEffect()).isEqualTo(PolicyEffect.BLOCK);
        assertThat(d.getRuleId()).isEqualTo("no-pii-in-prompts");
    }

    @Test
    void productionHostRuleIsInertUntilConfigured_thenBlocks() {
        String content = "Point the test at https://prod.example.com/checkout";
        assertThat(engine(defaults()).evaluate(content, ctx).getEffect())
                .as("no hosts configured → inert")
                .isEqualTo(PolicyEffect.ALLOW);

        ResponsibleAiPolicyProperties props = defaults();
        props.setProductionHosts(List.of("prod.example.com"));
        PolicyDecision d = engine(props).evaluate(content, ctx);
        assertThat(d.getEffect()).isEqualTo(PolicyEffect.BLOCK);
        assertThat(d.getRuleId()).isEqualTo("no-production-urls");
    }

    @Test
    void disablingPiiRuleAllowsPii() {
        ResponsibleAiPolicyProperties props = defaults();
        props.setBlockPii(false);
        PolicyDecision d = engine(props).evaluate("Contact carol@example.com", ctx);
        assertThat(d.getEffect()).isEqualTo(PolicyEffect.ALLOW);
    }

    @Test
    void customBlockPatternIsEnforced() {
        ResponsibleAiPolicyProperties props = defaults();
        props.setBlockPatterns(List.of("(?i)company-secret"));
        PolicyDecision d = engine(props).evaluate("This mentions COMPANY-SECRET data", ctx);
        assertThat(d.getEffect()).isEqualTo(PolicyEffect.BLOCK);
        assertThat(d.getRuleId()).startsWith("custom-block-");
    }
}
