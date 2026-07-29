package com.aiqaos.provider.cost;

import com.aiqaos.provider.model.LLMRequest;
import org.springframework.stereotype.Component;

/**
 * ENT-3: pre-flight cost-quota enforcement. Compares accumulated {@link SpendLedger} spend against
 * the configured {@link CostBudgetProperties} for the global / per-workflow / per-agent scopes.
 * Soft cap: a scope is blocked once its <em>already-recorded</em> spend has reached its limit.
 */
@Component
public class CostBudgetEnforcer {

    private final SpendLedger ledger;
    private final CostBudgetProperties properties;

    public CostBudgetEnforcer(SpendLedger ledger, CostBudgetProperties properties) {
        this.ledger = ledger;
        this.properties = properties;
    }

    public BudgetVerdict check(LLMRequest request) {
        if (!properties.isEnabled()) {
            return BudgetVerdict.allow();
        }

        Double global = properties.getGlobalDaily();
        if (global != null) {
            double spent = ledger.globalToday();
            if (spent >= global) {
                return BudgetVerdict.exceeded("global-daily", global, spent);
            }
        }

        Double perWorkflow = properties.getPerWorkflow();
        if (perWorkflow != null && request != null && request.getCorrelationId() != null) {
            double spent = ledger.workflow(request.getCorrelationId());
            if (spent >= perWorkflow) {
                return BudgetVerdict.exceeded("per-workflow", perWorkflow, spent);
            }
        }

        Double perAgent = properties.getPerAgent();
        if (perAgent != null && request != null && request.getAgentType() != null) {
            double spent = ledger.agent(request.getAgentType());
            if (spent >= perAgent) {
                return BudgetVerdict.exceeded("per-agent", perAgent, spent);
            }
        }

        return BudgetVerdict.allow();
    }

    public boolean isEnforce() {
        return properties.isEnforce();
    }
}
