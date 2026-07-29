package com.aiqaos.dashboard.service;

import com.aiqaos.dashboard.dto.HealingAnalyticsSummary;
import com.aiqaos.observability.entity.HealingMetricEntity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * HEAL-3: pure aggregation of healing metrics into a {@link HealingAnalyticsSummary}. No I/O — takes
 * the metric list and computes the read-model — so it is trivially unit-testable (the GOV-1
 * {@code AiAuditAssembler} pattern). {@code successRate} is successful heals over the total.
 */
@Component
public class HealingAnalyticsAssembler {

    public HealingAnalyticsSummary summarize(List<HealingMetricEntity> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return HealingAnalyticsSummary.empty();
        }

        long total = metrics.size();
        long applied = 0;
        long successful = 0;
        double improvementSum = 0.0;
        Map<String, Long> byAction = new LinkedHashMap<>();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        Map<String, Long> byCategory = new LinkedHashMap<>();

        for (HealingMetricEntity m : metrics) {
            if (m.isHealingApplied()) {
                applied++;
            }
            if (m.isRetrySuccessful()) {
                successful++;
            }
            improvementSum += m.getImprovementScore();
            bump(byAction, m.getActionType());
            bump(byStatus, m.getRecoveryStatus());
            bump(byCategory, m.getFailureCategory());
        }

        double successRate = (double) successful / total;
        double avgImprovement = improvementSum / total;

        return new HealingAnalyticsSummary(total, applied, successful, successRate, avgImprovement,
                byAction, byStatus, byCategory);
    }

    private static void bump(Map<String, Long> map, String key) {
        map.merge(key != null ? key : "UNKNOWN", 1L, Long::sum);
    }
}
