package com.aiqaos.learning.dashboard;

import com.aiqaos.learning.metrics.LearningMetrics;
import com.aiqaos.learning.metrics.LearningTrend;
import org.springframework.stereotype.Component;

/**
 * LRN-3: composes LRN-2's {@link LearningMetrics} into a {@link LearningDashboardView} for
 * presentation, deriving the {@link LearningHealth} signal and a human headline. Pure — no I/O — so
 * it is trivially unit-testable (the HEAL-3/GOV-1 assembler pattern).
 */
@Component
public class LearningDashboardAssembler {

    /** A learning score below this (absent a positive trend) is flagged at-risk. */
    static final double HEALTHY_SCORE_FLOOR = 0.5;

    public LearningDashboardView assemble(LearningMetrics metrics) {
        if (metrics == null) {
            return new LearningDashboardView(0.0, 0.0, 0.0, java.util.List.of(),
                    LearningTrend.STABLE, 0, LearningHealth.AT_RISK, "No learning data yet");
        }

        LearningHealth health = health(metrics);
        String headline = headline(metrics, health);

        return new LearningDashboardView(
                metrics.getLearningScore(), metrics.getSuccessRate(), metrics.getAvgConfidence(),
                metrics.getConfidenceHistory(), metrics.getTrend(), metrics.getSampleCount(),
                health, headline);
    }

    private LearningHealth health(LearningMetrics m) {
        boolean regressing = m.getTrend() == LearningTrend.REGRESSING;
        boolean lowScore = m.getLearningScore() < HEALTHY_SCORE_FLOOR;
        return (regressing || lowScore) ? LearningHealth.AT_RISK : LearningHealth.HEALTHY;
    }

    private String headline(LearningMetrics m, LearningHealth health) {
        String trendWord = switch (m.getTrend()) {
            case IMPROVING -> "improving";
            case REGRESSING -> "regressing";
            case STABLE -> "stable";
        };
        String prefix = health == LearningHealth.AT_RISK ? "Learning at risk" : "Learning healthy";
        return String.format("%s — %s (score %.2f)", prefix, trendWord, m.getLearningScore());
    }
}
