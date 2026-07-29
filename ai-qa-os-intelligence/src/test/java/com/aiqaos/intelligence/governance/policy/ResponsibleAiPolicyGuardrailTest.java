package com.aiqaos.intelligence.governance.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.guardrail.GuardrailContext;
import com.aiqaos.core.guardrail.GuardrailVerdict;
import org.junit.jupiter.api.Test;

/**
 * GOV-3: unit tests for the guardrail adapter — fail-safe mapping of policy decisions to guardrail
 * verdicts (BLOCK and REQUIRE_REVIEW both block), plus the enabled/mode gates. No Mockito.
 */
class ResponsibleAiPolicyGuardrailTest {

    private final GuardrailContext ctx = GuardrailContext.input("test");

    private ResponsibleAiPolicyGuardrail guardrail(ResponsibleAiPolicyProperties props) {
        return new ResponsibleAiPolicyGuardrail(new RuleBasedResponsibleAiPolicyEngine(props), props);
    }

    @Test
    void cleanContentIsAllowed() {
        GuardrailVerdict v = guardrail(new ResponsibleAiPolicyProperties())
                .check("Verify the dashboard renders", ctx);
        assertThat(v.isAllowed()).isTrue();
        assertThat(v.getAction()).isEqualTo(GuardrailVerdict.Action.ALLOW);
    }

    @Test
    void blockDecisionIsNotAllowed() {
        GuardrailVerdict v = guardrail(new ResponsibleAiPolicyProperties())
                .check("email dave@example.com", ctx);
        assertThat(v.isAllowed()).isFalse();
        assertThat(v.getAction()).isEqualTo(GuardrailVerdict.Action.BLOCK);
        assertThat(v.getReason()).contains("no-pii-in-prompts");
    }

    @Test
    void requireReviewIsFailSafeBlocked() {
        // Destructive → REQUIRE_REVIEW → blocked (fail-safe) until gate escalation is wired (FI-GOV3-B).
        GuardrailVerdict v = guardrail(new ResponsibleAiPolicyProperties())
                .check("rm -rf /var/data", ctx);
        assertThat(v.isAllowed()).isFalse();
        assertThat(v.getReason()).contains("destructive-requires-review");
    }

    @Test
    void disabledPolicyAllowsEverything() {
        ResponsibleAiPolicyProperties props = new ResponsibleAiPolicyProperties();
        props.setEnabled(false);
        GuardrailVerdict v = guardrail(props).check("email eve@example.com", ctx);
        assertThat(v.isAllowed()).isTrue();
    }

    @Test
    void warnModeAllowsButDoesNotThrow() {
        ResponsibleAiPolicyProperties props = new ResponsibleAiPolicyProperties();
        props.setMode("warn");
        GuardrailVerdict v = guardrail(props).check("DROP TABLE orders", ctx);
        assertThat(v.isAllowed()).as("warn mode logs and allows").isTrue();
    }
}
