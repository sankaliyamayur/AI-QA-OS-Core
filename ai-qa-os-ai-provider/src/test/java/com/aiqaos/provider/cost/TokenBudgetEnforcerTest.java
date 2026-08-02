package com.aiqaos.provider.cost;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.provider.model.LLMRequest;
import org.junit.jupiter.api.Test;

/** AI-6 (ADR-075): token-budget verdicts across scopes + the enabled/mode gates (soft cap, real counts). */
class TokenBudgetEnforcerTest {

    private static LLMRequest request(String correlationId, String agentType) {
        LLMRequest r = new LLMRequest();
        r.setCorrelationId(correlationId);
        r.setAgentType(agentType);
        return r;
    }

    private static TokenBudgetProperties props(boolean enabled, String mode, Long global, Long workflow, Long agent) {
        TokenBudgetProperties p = new TokenBudgetProperties();
        p.setEnabled(enabled);
        p.setMode(mode);
        p.setGlobalDailyTokens(global);
        p.setPerWorkflowTokens(workflow);
        p.setPerAgentTokens(agent);
        return p;
    }

    @Test
    void disabledAlwaysAllows() {
        TokenLedger ledger = new TokenLedger();
        ledger.record(10_000, "wf-1", "A");
        TokenBudgetEnforcer enforcer = new TokenBudgetEnforcer(ledger, props(false, "enforce", 1L, 1L, 1L));

        assertThat(enforcer.check(request("wf-1", "A")).isAllowed()).isTrue();
    }

    @Test
    void blocksWhenPerWorkflowLimitReached() {
        TokenLedger ledger = new TokenLedger();
        ledger.record(5_000, "wf-1", "A");
        TokenBudgetEnforcer enforcer = new TokenBudgetEnforcer(ledger, props(true, "enforce", null, 5_000L, null));

        BudgetVerdict v = enforcer.check(request("wf-1", "A"));
        assertThat(v.isAllowed()).isFalse();
        assertThat(v.getScope()).isEqualTo("per-workflow-tokens");
        assertThat(v.getLimit()).isEqualTo(5_000.0);
        assertThat(v.getSpend()).isEqualTo(5_000.0);
        assertThat(enforcer.isEnforce()).isTrue();
    }

    @Test
    void blocksOnGlobalAndAgentScopes() {
        TokenLedger ledger = new TokenLedger();
        ledger.record(2_000, "wf-1", "SCRIPT_GENERATOR");

        assertThat(new TokenBudgetEnforcer(ledger, props(true, "enforce", 2_000L, null, null))
                .check(request("wf-1", "SCRIPT_GENERATOR")).getScope()).isEqualTo("global-daily-tokens");
        assertThat(new TokenBudgetEnforcer(ledger, props(true, "enforce", null, null, 2_000L))
                .check(request("wf-1", "SCRIPT_GENERATOR")).getScope()).isEqualTo("per-agent-tokens");
    }

    @Test
    void allowsUnderLimitsAndReportsWarnMode() {
        TokenLedger ledger = new TokenLedger();
        ledger.record(100, "wf-1", "A");
        TokenBudgetEnforcer enforcer = new TokenBudgetEnforcer(ledger, props(true, "warn", 1_000L, 1_000L, 1_000L));

        assertThat(enforcer.check(request("wf-1", "A")).isAllowed()).isTrue();
        assertThat(enforcer.isEnforce()).isFalse();   // warn mode
    }

    @Test
    void unsetScopeIsUnlimited() {
        TokenLedger ledger = new TokenLedger();
        ledger.record(1_000_000, "wf-1", "A");   // huge usage, but no limits configured
        TokenBudgetEnforcer enforcer = new TokenBudgetEnforcer(ledger, props(true, "enforce", null, null, null));

        assertThat(enforcer.check(request("wf-1", "A")).isAllowed()).isTrue();
    }
}
