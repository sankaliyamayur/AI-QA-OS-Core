package com.aiqaos.learning.metrics;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * LRN-2: computes the learning metrics — Success Rate, Confidence History, a composite Learning
 * Score, and an improving/stable/regressing {@link LearningTrend} — from a time-ordered series of
 * {@link LearningObservation}s. Pure arithmetic: no I/O, no cross-module reads. Sourcing the
 * observations from real history and storing the result are deferred (FI-LRN2-A/B).
 */
@Component
public class LearningMetricsCalculator {

    private final LearningMetricsProperties props;

    public LearningMetricsCalculator(LearningMetricsProperties props) {
        this.props = props;
    }

    public LearningMetrics compute(List<LearningObservation> observations) {
        if (observations == null || observations.isEmpty()) {
            return LearningMetrics.empty();
        }
        int n = observations.size();

        int successes = 0;
        double confidenceSum = 0.0;
        List<Double> confidenceHistory = new ArrayList<>(n);
        for (LearningObservation o : observations) {
            if (o.isSuccess()) {
                successes++;
            }
            confidenceSum += o.getConfidence();
            confidenceHistory.add(o.getConfidence());
        }

        double successRate = (double) successes / n;
        double avgConfidence = confidenceSum / n;
        LearningTrend trend = computeTrend(observations);

        double trendBonus = switch (trend) {
            case IMPROVING -> 1.0;
            case STABLE -> 0.5;
            case REGRESSING -> 0.0;
        };
        double raw = props.getSuccessWeight() * successRate
                + props.getConfidenceWeight() * avgConfidence
                + props.getTrendWeight() * trendBonus;
        double learningScore = clamp01(raw);

        return new LearningMetrics(learningScore, successRate, avgConfidence, confidenceHistory, n, trend);
    }

    /** Compare the second half's success rate to the first half's (needs ≥ 2 samples). */
    private LearningTrend computeTrend(List<LearningObservation> obs) {
        int n = obs.size();
        if (n < 2) {
            return LearningTrend.STABLE;
        }
        int mid = n / 2;
        double firstHalf = successRate(obs.subList(0, mid));
        double secondHalf = successRate(obs.subList(mid, n));
        double delta = secondHalf - firstHalf;
        if (delta > props.getTrendEpsilon()) {
            return LearningTrend.IMPROVING;
        }
        if (delta < -props.getTrendEpsilon()) {
            return LearningTrend.REGRESSING;
        }
        return LearningTrend.STABLE;
    }

    private double successRate(List<LearningObservation> obs) {
        if (obs.isEmpty()) {
            return 0.0;
        }
        int s = 0;
        for (LearningObservation o : obs) {
            if (o.isSuccess()) {
                s++;
            }
        }
        return (double) s / obs.size();
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
