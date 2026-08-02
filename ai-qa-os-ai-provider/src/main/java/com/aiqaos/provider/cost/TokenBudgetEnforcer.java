package com.aiqaos.provider.cost;

import com.aiqaos.provider.model.LLMRequest;
import org.springframework.stereotype.Component;

/**
 * AI-6 (ADR-075): pre-flight token/context-budget enforcement — the token counterpart to
 * {@link CostBudgetEnforcer}. Compares accumulated {@link TokenLedger} usage against the configured
 * {@link TokenBudgetProperties} for the global / per-workflow / per-agent scopes. Soft cap: a scope is
 * blocked once its <em>already-recorded actual</em> token usage has reached its limit. Reuses
 * {@link BudgetVerdict} (limit/used carried as doubles; scope label distinguishes the token scopes).
 */
@Component
public class TokenBudgetEnforcer {

    private final TokenLedger ledger;
    private final TokenBudgetProperties properties;

    public TokenBudgetEnforcer(TokenLedger ledger, TokenBudgetProperties properties) {
        this.ledger = ledger;
        this.properties = properties;
    }

    public BudgetVerdict check(LLMRequest request) {
        if (!properties.isEnabled()) {
            return BudgetVerdict.allow();
        }

        Long global = properties.getGlobalDailyTokens();
        if (global != null) {
            long used = ledger.globalToday();
            if (used >= global) {
                return BudgetVerdict.exceeded("global-daily-tokens", (double) global, used);
            }
        }

        Long perWorkflow = properties.getPerWorkflowTokens();
        if (perWorkflow != null && request != null && request.getCorrelationId() != null) {
            long used = ledger.workflow(request.getCorrelationId());
            if (used >= perWorkflow) {
                return BudgetVerdict.exceeded("per-workflow-tokens", (double) perWorkflow, used);
            }
        }

        Long perAgent = properties.getPerAgentTokens();
        if (perAgent != null && request != null && request.getAgentType() != null) {
            long used = ledger.agent(request.getAgentType());
            if (used >= perAgent) {
                return BudgetVerdict.exceeded("per-agent-tokens", (double) perAgent, used);
            }
        }

        return BudgetVerdict.allow();
    }

    public boolean isEnforce() {
        return properties.isEnforce();
    }
}
