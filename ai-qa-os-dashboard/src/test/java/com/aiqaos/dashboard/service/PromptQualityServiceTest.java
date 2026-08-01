package com.aiqaos.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aiqaos.eval.benchmark.PromptQualityAssembler;
import com.aiqaos.eval.benchmark.PromptQualitySummary;
import com.aiqaos.eval.benchmark.PromptRegressionAnalyzer;
import com.aiqaos.eval.benchmark.PromptRegressionReport;
import com.aiqaos.eval.entity.EvaluationResultEntity;
import com.aiqaos.eval.repository.EvaluationResultRepository;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * PE-3 (ADR-062): the service aggregates persisted eval results into a per-version leaderboard
 * (mean score, ranked) and summarizes it. FI-PE3-B (ADR-069): also detects temporal regressions.
 * Mockito-free (JDK 25).
 */
class PromptQualityServiceTest {

    private PromptQualityService service(EvaluationResultRepository repo) {
        return new PromptQualityService(repo, new PromptQualityAssembler(),
                new PromptRegressionAnalyzer(), 0.05, 4);
    }

    @Test
    void getSummary_aggregatesMeanScorePerVersionThenRanks() {
        // v1 mean = (0.9 + 0.7)/2 = 0.8 ; v2 = 0.6
        EvaluationResultRepository repo = repoReturning(List.of(
                result("v1", 0.9), result("v1", 0.7), result("v2", 0.6)));

        PromptQualitySummary summary = service(repo).getSummary();

        assertEquals(2, summary.getTotalVersions(), "two distinct versions");
        assertEquals("v1", summary.getBestVersionId(), "v1 has the higher mean");
        assertEquals(0.8, summary.getBestScore(), 1e-9);
        assertEquals("v2", summary.getWorstVersionId());
        assertEquals(0.6, summary.getWorstScore(), 1e-9);
        assertEquals(0.7, summary.getAverageScore(), 1e-9, "mean of 0.8 and 0.6");
    }

    @Test
    void getSummary_emptyWhenNoResults() {
        assertEquals(0, service(repoReturning(List.of())).getSummary().getTotalVersions());
    }

    @Test
    void getRegressions_flagsVersionDecliningOverTime_skipsInsufficientData() {
        // v1: earlier (older createdTime) ~0.9, recent ~0.4 -> regressed; v2: only 2 results -> skipped
        EvaluationResultRepository repo = repoReturning(List.of(
                timed("v1", 0.9, LocalDateTime.of(2026, 1, 1, 0, 0)),
                timed("v1", 0.9, LocalDateTime.of(2026, 1, 2, 0, 0)),
                timed("v1", 0.4, LocalDateTime.of(2026, 1, 3, 0, 0)),
                timed("v1", 0.4, LocalDateTime.of(2026, 1, 4, 0, 0)),
                timed("v2", 0.9, LocalDateTime.of(2026, 1, 1, 0, 0)),
                timed("v2", 0.2, LocalDateTime.of(2026, 1, 2, 0, 0))));

        PromptRegressionReport report = service(repo).getRegressions();

        assertEquals(1, report.regressedCount(), "only v1 has enough data and declined");
        assertEquals("v1", report.regressions().get(0).versionId());
        assertEquals(0.9, report.regressions().get(0).baselineScore(), 1e-9);
        assertEquals(0.4, report.regressions().get(0).currentScore(), 1e-9);
    }

    private static EvaluationResultEntity result(String version, double score) {
        return timed(version, score, null);
    }

    private static EvaluationResultEntity timed(String version, double score, LocalDateTime createdTime) {
        EvaluationResultEntity e = new EvaluationResultEntity();
        e.setPromptVersion(version);
        e.setScore(score);
        e.setCreatedTime(createdTime);
        return e;
    }

    private static EvaluationResultRepository repoReturning(List<EvaluationResultEntity> all) {
        return (EvaluationResultRepository) Proxy.newProxyInstance(
                PromptQualityServiceTest.class.getClassLoader(),
                new Class<?>[]{EvaluationResultRepository.class},
                (proxy, method, args) -> {
                    if ("findAll".equals(method.getName()) && (args == null || args.length == 0)) {
                        return all;
                    }
                    Class<?> rt = method.getReturnType();
                    if (rt == boolean.class) return false;
                    if (rt == long.class) return 0L;
                    if (rt == Optional.class) return Optional.empty();
                    if (rt == List.class) return List.of();
                    return null;
                });
    }
}
