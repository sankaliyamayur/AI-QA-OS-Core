package com.aiqaos.provider.cost;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ENT-3: LLM cost-quota configuration. Disabled by default (non-breaking); when enabled, limits are
 * in USD and an unset limit means "unlimited" for that scope. {@code mode} is {@code enforce} (block)
 * or {@code warn} (log only).
 */
@Component
@ConfigurationProperties(prefix = "aiqaos.cost.quota")
public class CostBudgetProperties {

    private boolean enabled = false;
    private String mode = "enforce";
    private Double globalDaily;
    private Double perWorkflow;
    private Double perAgent;

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

    public Double getGlobalDaily() {
        return globalDaily;
    }

    public void setGlobalDaily(Double globalDaily) {
        this.globalDaily = globalDaily;
    }

    public Double getPerWorkflow() {
        return perWorkflow;
    }

    public void setPerWorkflow(Double perWorkflow) {
        this.perWorkflow = perWorkflow;
    }

    public Double getPerAgent() {
        return perAgent;
    }

    public void setPerAgent(Double perAgent) {
        this.perAgent = perAgent;
    }
}
