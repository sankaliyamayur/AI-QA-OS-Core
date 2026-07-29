package com.aiqaos.learning.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LRN-2: the computed learning metrics — a composite {@code learningScore} (0..1), the
 * {@code successRate}, the {@code confidenceHistory} series (for LRN-3 to plot), the average
 * confidence, the sample count, and the overall {@link LearningTrend}.
 */
public final class LearningMetrics {

    private final double learningScore;
    private final double successRate;
    private final double avgConfidence;
    private final List<Double> confidenceHistory;
    private final int sampleCount;
    private final LearningTrend trend;

    public LearningMetrics(double learningScore, double successRate, double avgConfidence,
                           List<Double> confidenceHistory, int sampleCount, LearningTrend trend) {
        this.learningScore = learningScore;
        this.successRate = successRate;
        this.avgConfidence = avgConfidence;
        this.confidenceHistory = confidenceHistory != null
                ? Collections.unmodifiableList(new ArrayList<>(confidenceHistory))
                : Collections.emptyList();
        this.sampleCount = sampleCount;
        this.trend = trend;
    }

    public static LearningMetrics empty() {
        return new LearningMetrics(0.0, 0.0, 0.0, Collections.emptyList(), 0, LearningTrend.STABLE);
    }

    public double getLearningScore() { return learningScore; }
    public double getSuccessRate() { return successRate; }
    public double getAvgConfidence() { return avgConfidence; }
    public List<Double> getConfidenceHistory() { return confidenceHistory; }
    public int getSampleCount() { return sampleCount; }
    public LearningTrend getTrend() { return trend; }

    @Override
    public String toString() {
        return String.format("LearningMetrics{score=%.2f, successRate=%.2f, avgConf=%.2f, n=%d, %s}",
                learningScore, successRate, avgConfidence, sampleCount, trend);
    }
}
