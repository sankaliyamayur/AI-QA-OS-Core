package com.aiqaos.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.aiqaos.dashboard.dto.HealingAnalyticsSummary;
import com.aiqaos.observability.entity.HealingMetricEntity;
import java.util.List;
import org.junit.jupiter.api.Test;

/** HEAL-3: unit tests for pure healing-analytics aggregation. No Mockito. */
class HealingAnalyticsAssemblerTest {

    private final HealingAnalyticsAssembler assembler = new HealingAnalyticsAssembler();

    private HealingMetricEntity metric(String action, String status, String category,
                                       boolean applied, boolean successful, double improvement) {
        HealingMetricEntity m = new HealingMetricEntity();
        m.setActionType(action);
        m.setRecoveryStatus(status);
        m.setFailureCategory(category);
        m.setHealingApplied(applied);
        m.setRetrySuccessful(successful);
        m.setImprovementScore(improvement);
        return m;
    }

    private List<HealingMetricEntity> sample() {
        return List.of(
                metric("LOCATOR_UPDATE", "SUCCESS", "GENERIC_ERROR", true, true, 1.0),
                metric("LOCATOR_UPDATE", "FAILED", "GENERIC_ERROR", true, false, 0.0),
                metric("SCRIPT_REGENERATE", "SUCCESS", "LOCATOR_ERROR", true, true, 1.0),
                metric("RETRY_ONLY", "MAX_ATTEMPTS_EXCEEDED", "TIMEOUT", false, false, 0.0));
    }

    @Test
    void computesHeadlineCountsAndSuccessRate() {
        HealingAnalyticsSummary s = assembler.summarize(sample());
        assertThat(s.getTotal()).isEqualTo(4);
        assertThat(s.getAppliedCount()).isEqualTo(3);
        assertThat(s.getSuccessfulCount()).isEqualTo(2);
        assertThat(s.getSuccessRate()).isCloseTo(0.5, within(1e-9));
        assertThat(s.getAvgImprovementScore()).isCloseTo(0.5, within(1e-9));
    }

    @Test
    void breaksDownByActionStatusAndCategory() {
        HealingAnalyticsSummary s = assembler.summarize(sample());
        assertThat(s.getActionTypeBreakdown())
                .containsEntry("LOCATOR_UPDATE", 2L)
                .containsEntry("SCRIPT_REGENERATE", 1L)
                .containsEntry("RETRY_ONLY", 1L);
        assertThat(s.getRecoveryStatusBreakdown())
                .containsEntry("SUCCESS", 2L).containsEntry("FAILED", 1L)
                .containsEntry("MAX_ATTEMPTS_EXCEEDED", 1L);
        assertThat(s.getFailureCategoryBreakdown())
                .containsEntry("GENERIC_ERROR", 2L).containsEntry("LOCATOR_ERROR", 1L)
                .containsEntry("TIMEOUT", 1L);
    }

    @Test
    void emptyInputYieldsZeroedSummary() {
        HealingAnalyticsSummary s = assembler.summarize(List.of());
        assertThat(s.getTotal()).isZero();
        assertThat(s.getSuccessRate()).isZero();
        assertThat(s.getActionTypeBreakdown()).isEmpty();
    }

    @Test
    void nullActionTypeIsBucketedAsUnknown() {
        HealingAnalyticsSummary s = assembler.summarize(List.of(
                metric(null, "SUCCESS", null, true, true, 1.0)));
        assertThat(s.getActionTypeBreakdown()).containsEntry("UNKNOWN", 1L);
        assertThat(s.getFailureCategoryBreakdown()).containsEntry("UNKNOWN", 1L);
    }
}
