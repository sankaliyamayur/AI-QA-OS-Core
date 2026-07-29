package com.aiqaos.learning.metrics;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LRN-2: learning-metrics configuration ({@code aiqaos.learning.metrics.*}). The Learning Score is a
 * weighted blend of success rate, average confidence, and a trend bonus; {@code trendEpsilon} is the
 * minimum first-half→second-half success-rate change that counts as a real trend.
 */
@Component
@ConfigurationProperties(prefix = "aiqaos.learning.metrics")
public class LearningMetricsProperties {

    private double successWeight = 0.5;
    private double confidenceWeight = 0.3;
    private double trendWeight = 0.2;
    private double trendEpsilon = 0.05;

    public double getSuccessWeight() { return successWeight; }
    public void setSuccessWeight(double successWeight) { this.successWeight = successWeight; }

    public double getConfidenceWeight() { return confidenceWeight; }
    public void setConfidenceWeight(double confidenceWeight) { this.confidenceWeight = confidenceWeight; }

    public double getTrendWeight() { return trendWeight; }
    public void setTrendWeight(double trendWeight) { this.trendWeight = trendWeight; }

    public double getTrendEpsilon() { return trendEpsilon; }
    public void setTrendEpsilon(double trendEpsilon) { this.trendEpsilon = trendEpsilon; }
}
