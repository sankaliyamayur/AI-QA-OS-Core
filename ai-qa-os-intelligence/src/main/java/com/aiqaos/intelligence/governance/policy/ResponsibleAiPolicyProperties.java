package com.aiqaos.intelligence.governance.policy;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * GOV-3: Responsible-AI policy configuration ({@code aiqaos.governance.policy.*}). Every default
 * rule is overridable: toggle PII / destructive checks, supply the production hosts that must never
 * appear in AI content, and add arbitrary block patterns. Enabled by default in {@code enforce}
 * mode, mirroring the SEC-3 guardrail gate.
 */
@Component
@ConfigurationProperties(prefix = "aiqaos.governance.policy")
public class ResponsibleAiPolicyProperties {

    /** Master switch; when false the guardrail allows everything (non-breaking off-ramp). */
    private boolean enabled = true;

    /** {@code enforce} (block violations) or {@code warn} (log only, allow). */
    private String mode = "enforce";

    /** Hosts that must never appear in AI content (e.g. {@code prod.example.com}). Empty = rule inert. */
    private List<String> productionHosts = new ArrayList<>();

    /** {@code no-pii-in-prompts}: block emails / card-shaped / SSN-shaped numbers. */
    private boolean blockPii = true;

    /** {@code destructive-requires-review}: flag destructive-action keywords for review. */
    private boolean reviewDestructive = true;

    /** Extra operator-supplied regexes; each becomes a BLOCK rule. */
    private List<String> blockPatterns = new ArrayList<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    /** True unless {@code mode} is explicitly {@code warn}. */
    public boolean isEnforce() { return !"warn".equalsIgnoreCase(mode); }

    public List<String> getProductionHosts() { return productionHosts; }
    public void setProductionHosts(List<String> productionHosts) { this.productionHosts = productionHosts; }

    public boolean isBlockPii() { return blockPii; }
    public void setBlockPii(boolean blockPii) { this.blockPii = blockPii; }

    public boolean isReviewDestructive() { return reviewDestructive; }
    public void setReviewDestructive(boolean reviewDestructive) { this.reviewDestructive = reviewDestructive; }

    public List<String> getBlockPatterns() { return blockPatterns; }
    public void setBlockPatterns(List<String> blockPatterns) { this.blockPatterns = blockPatterns; }
}
