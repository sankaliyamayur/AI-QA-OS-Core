package com.aiqaos.healing.approval;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HEAL-2: approval-workflow configuration ({@code aiqaos.healing.approval.*}). The thresholds are the
 * <em>gate-absent</em> fallback used when the AI-1 confidence gate is not on the classpath: at/above
 * {@code autoApprove} auto-applies, at/above {@code reviewFloor} needs approval, below is rejected.
 * When the gate is present, it decides instead.
 */
@Component
@ConfigurationProperties(prefix = "aiqaos.healing.approval")
public class HealingApprovalProperties {

    private double autoApprove = 0.90;
    private double reviewFloor = 0.50;

    public double getAutoApprove() { return autoApprove; }
    public void setAutoApprove(double autoApprove) { this.autoApprove = autoApprove; }

    public double getReviewFloor() { return reviewFloor; }
    public void setReviewFloor(double reviewFloor) { this.reviewFloor = reviewFloor; }
}
