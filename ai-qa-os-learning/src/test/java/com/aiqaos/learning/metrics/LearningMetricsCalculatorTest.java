package com.aiqaos.learning.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** LRN-2: unit tests for the learning-metrics computation (success rate, trend, score, history). */
class LearningMetricsCalculatorTest {

    private final LearningMetricsCalculator calc =
            new LearningMetricsCalculator(new LearningMetricsProperties());

    private List<LearningObservation> series(boolean[] outcomes, double conf) {
        List<LearningObservation> obs = new ArrayList<>();
        for (int i = 0; i < outcomes.length; i++) {
            obs.add(LearningObservation.of(i, outcomes[i], conf));
        }
        return obs;
    }

    @Test
    void emptyInputYieldsEmptyStableMetrics() {
        LearningMetrics m = calc.compute(List.of());
        assertThat(m.getSampleCount()).isZero();
        assertThat(m.getLearningScore()).isZero();
        assertThat(m.getTrend()).isEqualTo(LearningTrend.STABLE);
    }

    @Test
    void successRateIsComputedCorrectly() {
        LearningMetrics m = calc.compute(series(new boolean[]{true, true, true, false, false}, 0.8));
        assertThat(m.getSuccessRate()).isEqualTo(0.6);
        assertThat(m.getSampleCount()).isEqualTo(5);
    }

    @Test
    void improvingSeriesIsDetected() {
        LearningMetrics m = calc.compute(series(new boolean[]{false, false, true, true}, 0.7));
        assertThat(m.getTrend()).isEqualTo(LearningTrend.IMPROVING);
    }

    @Test
    void regressingSeriesIsDetected() {
        LearningMetrics m = calc.compute(series(new boolean[]{true, true, false, false}, 0.7));
        assertThat(m.getTrend()).isEqualTo(LearningTrend.REGRESSING);
    }

    @Test
    void stableSeriesIsDetected() {
        LearningMetrics m = calc.compute(series(new boolean[]{true, false, true, false}, 0.7));
        assertThat(m.getTrend()).isEqualTo(LearningTrend.STABLE);
    }

    @Test
    void confidenceHistoryPreservesOrder() {
        List<LearningObservation> obs = List.of(
                LearningObservation.of(0, true, 0.1),
                LearningObservation.of(1, true, 0.5),
                LearningObservation.of(2, false, 0.9));
        LearningMetrics m = calc.compute(obs);
        assertThat(m.getConfidenceHistory()).containsExactly(0.1, 0.5, 0.9);
        assertThat(m.getAvgConfidence()).isCloseTo(0.5, within(1e-9));
    }

    @Test
    void perfectRunScoresHighAndWithinUnitRange() {
        LearningMetrics m = calc.compute(series(new boolean[]{false, false, true, true}, 1.0));
        // successRate 0.5, avgConf 1.0, IMPROVING → 0.5*0.5 + 0.3*1.0 + 0.2*1.0 = 0.75
        assertThat(m.getLearningScore()).isCloseTo(0.75, within(1e-9));
        assertThat(m.getLearningScore()).isBetween(0.0, 1.0);
    }
}
