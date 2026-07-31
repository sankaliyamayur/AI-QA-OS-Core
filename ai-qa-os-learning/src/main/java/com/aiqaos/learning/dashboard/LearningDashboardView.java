package com.aiqaos.learning.dashboard;

import com.aiqaos.learning.metrics.LearningTrend;
import java.util.Collections;
import java.util.List;

/**
 * LRN-3: the presentation view of the learning loop's health, composed from LRN-2's
 * {@code LearningMetrics} — the headline score + rates, the confidence-history series to plot, the
 * {@link LearningTrend}, an at-a-glance {@link LearningHealth} signal, and a human headline.
 */
public final class LearningDashboardView {

    private final double learningScore;
    private final double successRate;
    private final double avgConfidence;
    private final List<Double> confidenceHistory;
    private final LearningTrend trend;
    private final int sampleCount;
    private final LearningHealth health;
    private final String headline;

    public LearningDashboardView(double learningScore, double successRate, double avgConfidence,
                                 List<Double> confidenceHistory, LearningTrend trend, int sampleCount,
                                 LearningHealth health, String headline) {
        this.learningScore = learningScore;
        this.successRate = successRate;
        this.avgConfidence = avgConfidence;
        this.confidenceHistory = confidenceHistory != null
                ? Collections.unmodifiableList(List.copyOf(confidenceHistory))
                : List.of();
        this.trend = trend;
        this.sampleCount = sampleCount;
        this.health = health;
        this.headline = headline;
    }

    public double getLearningScore() { return learningScore; }
    public double getSuccessRate() { return successRate; }
    public double getAvgConfidence() { return avgConfidence; }
    public List<Double> getConfidenceHistory() { return confidenceHistory; }
    public LearningTrend getTrend() { return trend; }
    public int getSampleCount() { return sampleCount; }
    public LearningHealth getHealth() { return health; }
    public String getHeadline() { return headline; }
}
