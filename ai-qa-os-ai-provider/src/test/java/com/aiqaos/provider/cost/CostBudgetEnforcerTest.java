package com.aiqaos.provider.cost;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.provider.model.LLMRequest;
import org.junit.jupiter.api.Test;

/** ENT-3: quota verdicts across scopes + the enabled/mode gates. */
class CostBudgetEnforcerTest {

    private static LLMRequest request(String correlationId, String agentType) {
        LLMRequest r = new LLMRequest();
        r.setCorrelationId(correlationId);
        r.setAgentType(agentType);
        return r;
    }

    private static CostBudgetProperties props(boolean enabled, String mode, Double global, Double workflow, Double agent) {
        CostBudgetProperties p = new CostBudgetProperties();
        p.setEnabled(enabled);
        p.setMode(mode);
        p.setGlobalDaily(global);
        p.setPerWorkflow(workflow);
        p.setPerAgent(agent);
        return p;
    }

    @Test
    void disabledAlwaysAllows() {
        SpendLedger ledger = new SpendLedger();
        ledger.record(100.0, "wf-1", "A");
        CostBudgetEnforcer enforcer = new CostBudgetEnforcer(ledger, props(false, "enforce", 1.0, 1.0, 1.0));

        assertThat(enforcer.check(request("wf-1", "A")).isAllowed()).isTrue();
    }

    @Test
    void blocksWhenPerWorkflowLimitReached() {
        SpendLedger ledger = new SpendLedger();
        ledger.record(0.50, "wf-1", "A");
        CostBudgetEnforcer enforcer = new CostBudgetEnforcer(ledger, props(true, "enforce", null, 0.50, null));

        BudgetVerdict v = enforcer.check(request("wf-1", "A"));
        assertThat(v.isAllowed()).isFalse();
        assertThat(v.getScope()).isEqualTo("per-workflow");
        assertThat(enforcer.isEnforce()).isTrue();
    }

    @Test
    void blocksOnGlobalAndAgentScopes() {
        SpendLedger ledger = new SpendLedger();
        ledger.record(2.0, "wf-1", "SCRIPT_GENERATOR");

        assertThat(new CostBudgetEnforcer(ledger, props(true, "enforce", 2.0, null, null))
                .check(request("wf-1", "SCRIPT_GENERATOR")).getScope()).isEqualTo("global-daily");
        assertThat(new CostBudgetEnforcer(ledger, props(true, "enforce", null, null, 2.0))
                .check(request("wf-1", "SCRIPT_GENERATOR")).getScope()).isEqualTo("per-agent");
    }

    @Test
    void allowsUnderLimitsAndReportsWarnMode() {
        SpendLedger ledger = new SpendLedger();
        ledger.record(0.10, "wf-1", "A");
        CostBudgetEnforcer enforcer = new CostBudgetEnforcer(ledger, props(true, "warn", 1.0, 1.0, 1.0));

        assertThat(enforcer.check(request("wf-1", "A")).isAllowed()).isTrue();
        assertThat(enforcer.isEnforce()).isFalse();   // warn mode
    }
}
