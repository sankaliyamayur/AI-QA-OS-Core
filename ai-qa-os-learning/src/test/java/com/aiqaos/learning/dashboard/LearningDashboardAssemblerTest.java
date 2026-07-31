package com.aiqaos.learning.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.learning.metrics.LearningMetrics;
import com.aiqaos.learning.metrics.LearningTrend;
import java.util.List;
import org.junit.jupiter.api.Test;

/** LRN-3: unit tests for composing LRN-2 metrics into a dashboard view + health signal. No Mockito. */
class LearningDashboardAssemblerTest {

    private final LearningDashboardAssembler assembler = new LearningDashboardAssembler();

    private LearningMetrics metrics(double score, LearningTrend trend, List<Double> history) {
        return new LearningMetrics(score, 0.8, 0.85, history, history.size(), trend);
    }

    @Test
    void improvingWithGoodScoreIsHealthy() {
        LearningDashboardView v = assembler.assemble(
                metrics(0.75, LearningTrend.IMPROVING, List.of(0.8, 0.9)));
        assertThat(v.getHealth()).isEqualTo(LearningHealth.HEALTHY);
        assertThat(v.getHeadline()).containsIgnoringCase("healthy").containsIgnoringCase("improving");
        assertThat(v.getLearningScore()).isEqualTo(0.75);
    }

    @Test
    void regressingIsAtRiskEvenWithAGoodScore() {
        LearningDashboardView v = assembler.assemble(
                metrics(0.85, LearningTrend.REGRESSING, List.of()));
        assertThat(v.getHealth()).isEqualTo(LearningHealth.AT_RISK);
        assertThat(v.getHeadline()).containsIgnoringCase("at risk").containsIgnoringCase("regressing");
    }

    @Test
    void lowScoreIsAtRiskEvenWhenStable() {
        LearningDashboardView v = assembler.assemble(
                metrics(0.40, LearningTrend.STABLE, List.of()));
        assertThat(v.getHealth()).isEqualTo(LearningHealth.AT_RISK);
    }

    @Test
    void stableWithGoodScoreIsHealthy() {
        LearningDashboardView v = assembler.assemble(
                metrics(0.70, LearningTrend.STABLE, List.of(0.7, 0.8)));
        assertThat(v.getHealth()).isEqualTo(LearningHealth.HEALTHY);
    }

    @Test
    void confidenceHistorySeriesIsCarriedThrough() {
        LearningDashboardView v = assembler.assemble(
                metrics(0.7, LearningTrend.IMPROVING, List.of(0.1, 0.5, 0.9)));
        assertThat(v.getConfidenceHistory()).containsExactly(0.1, 0.5, 0.9);
    }

    @Test
    void nullMetricsYieldsAtRiskPlaceholder() {
        LearningDashboardView v = assembler.assemble(null);
        assertThat(v.getHealth()).isEqualTo(LearningHealth.AT_RISK);
        assertThat(v.getHeadline()).contains("No learning data");
    }
}
