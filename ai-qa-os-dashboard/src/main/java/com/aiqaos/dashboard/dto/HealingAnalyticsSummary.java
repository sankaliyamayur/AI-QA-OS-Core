package com.aiqaos.dashboard.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HEAL-3: the healing analytics read-model — headline counts, the heal <b>success rate</b>, the
 * average improvement score, and breakdowns by action type / recovery status / failure category.
 * Makes autonomous editing auditable at a glance.
 */
public final class HealingAnalyticsSummary {

    private final long total;
    private final long appliedCount;
    private final long successfulCount;
    private final double successRate;      // successfulCount / total (0..1)
    private final double avgImprovementScore;
    private final Map<String, Long> actionTypeBreakdown;
    private final Map<String, Long> recoveryStatusBreakdown;
    private final Map<String, Long> failureCategoryBreakdown;

    public HealingAnalyticsSummary(long total, long appliedCount, long successfulCount,
                                   double successRate, double avgImprovementScore,
                                   Map<String, Long> actionTypeBreakdown,
                                   Map<String, Long> recoveryStatusBreakdown,
                                   Map<String, Long> failureCategoryBreakdown) {
        this.total = total;
        this.appliedCount = appliedCount;
        this.successfulCount = successfulCount;
        this.successRate = successRate;
        this.avgImprovementScore = avgImprovementScore;
        this.actionTypeBreakdown = immutable(actionTypeBreakdown);
        this.recoveryStatusBreakdown = immutable(recoveryStatusBreakdown);
        this.failureCategoryBreakdown = immutable(failureCategoryBreakdown);
    }

    private static Map<String, Long> immutable(Map<String, Long> m) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(m != null ? m : Map.of()));
    }

    public static HealingAnalyticsSummary empty() {
        return new HealingAnalyticsSummary(0, 0, 0, 0.0, 0.0, Map.of(), Map.of(), Map.of());
    }

    public long getTotal() { return total; }
    public long getAppliedCount() { return appliedCount; }
    public long getSuccessfulCount() { return successfulCount; }
    public double getSuccessRate() { return successRate; }
    public double getAvgImprovementScore() { return avgImprovementScore; }
    public Map<String, Long> getActionTypeBreakdown() { return actionTypeBreakdown; }
    public Map<String, Long> getRecoveryStatusBreakdown() { return recoveryStatusBreakdown; }
    public Map<String, Long> getFailureCategoryBreakdown() { return failureCategoryBreakdown; }
}
