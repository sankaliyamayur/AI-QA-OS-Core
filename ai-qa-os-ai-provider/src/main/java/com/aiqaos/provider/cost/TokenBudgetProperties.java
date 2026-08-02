package com.aiqaos.provider.cost;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI-6 (ADR-075): LLM token/context-budget configuration — the token counterpart to
 * {@link CostBudgetProperties}. Disabled by default (non-breaking); limits are in <b>tokens</b> and an
 * unset limit means "unlimited" for that scope. {@code mode} is {@code enforce} (block) or {@code warn}
 * (log only). Budgets are per-workflow (correlation id), per-agent, and global-daily.
 */
@Component
@ConfigurationProperties(prefix = "aiqaos.context.budget")
public class TokenBudgetProperties {

    private boolean enabled = false;
    private String mode = "enforce";
    private Long globalDailyTokens;
    private Long perWorkflowTokens;
    private Long perAgentTokens;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isEnforce() {
        return !"warn".equalsIgnoreCase(mode);
    }

    public Long getGlobalDailyTokens() {
        return globalDailyTokens;
    }

    public void setGlobalDailyTokens(Long globalDailyTokens) {
        this.globalDailyTokens = globalDailyTokens;
    }

    public Long getPerWorkflowTokens() {
        return perWorkflowTokens;
    }

    public void setPerWorkflowTokens(Long perWorkflowTokens) {
        this.perWorkflowTokens = perWorkflowTokens;
    }

    public Long getPerAgentTokens() {
        return perAgentTokens;
    }

    public void setPerAgentTokens(Long perAgentTokens) {
        this.perAgentTokens = perAgentTokens;
    }
}
