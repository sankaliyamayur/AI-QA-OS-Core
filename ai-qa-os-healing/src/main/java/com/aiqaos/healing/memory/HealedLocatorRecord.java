package com.aiqaos.healing.memory;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * HEAL-4: a durable record of a validated locator heal — the {@code brokenLocator} that failed, the
 * {@code healedLocator} that replaced it (and its {@code strategy}/{@code confidence}), how often it
 * has been reused, whether the element is {@code fragile} (has drifted more than once), and the
 * {@code tenantId} it belongs to. Stored in the {@code memory} {@code MemoryStore}.
 */
public final class HealedLocatorRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String brokenLocator;
    private final String healedLocator;
    private final String strategy;
    private final double confidence;
    private final int reuseCount;
    private final boolean fragile;
    private final String tenantId;
    private final LocalDateTime lastHealedAt;

    public HealedLocatorRecord(String brokenLocator, String healedLocator, String strategy,
                               double confidence, int reuseCount, boolean fragile, String tenantId,
                               LocalDateTime lastHealedAt) {
        this.brokenLocator = brokenLocator;
        this.healedLocator = healedLocator;
        this.strategy = strategy;
        this.confidence = confidence;
        this.reuseCount = reuseCount;
        this.fragile = fragile;
        this.tenantId = tenantId;
        this.lastHealedAt = lastHealedAt;
    }

    public String getBrokenLocator() { return brokenLocator; }
    public String getHealedLocator() { return healedLocator; }
    public String getStrategy() { return strategy; }
    public double getConfidence() { return confidence; }
    public int getReuseCount() { return reuseCount; }
    public boolean isFragile() { return fragile; }
    public String getTenantId() { return tenantId; }
    public LocalDateTime getLastHealedAt() { return lastHealedAt; }

    @Override
    public String toString() {
        return "HealedLocatorRecord{" + brokenLocator + " → " + healedLocator
                + " reuse=" + reuseCount + (fragile ? " FRAGILE" : "") + " tenant=" + tenantId + "}";
    }
}
