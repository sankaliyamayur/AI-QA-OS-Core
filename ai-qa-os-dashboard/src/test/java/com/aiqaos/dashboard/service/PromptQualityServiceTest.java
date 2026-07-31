package com.aiqaos.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aiqaos.eval.benchmark.PromptQualityAssembler;
import com.aiqaos.eval.benchmark.PromptQualitySummary;
import com.aiqaos.eval.entity.EvaluationResultEntity;
import com.aiqaos.eval.repository.EvaluationResultRepository;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * PE-3 (ADR-062): the service aggregates persisted eval results into a per-version leaderboard
 * (mean score, ranked) and summarizes it. Mockito-free (JDK 25).
 */
class PromptQualityServiceTest {

    @Test
    void getSummary_aggregatesMeanScorePerVersionThenRanks() {
        // v1 mean = (0.9 + 0.7)/2 = 0.8 ; v2 = 0.6
        EvaluationResultRepository repo = repoReturning(List.of(
                result("v1", 0.9), result("v1", 0.7), result("v2", 0.6)));

        PromptQualityService service = new PromptQualityService(repo, new PromptQualityAssembler());
        PromptQualitySummary summary = service.getSummary();

        assertEquals(2, summary.getTotalVersions(), "two distinct versions");
        assertEquals("v1", summary.getBestVersionId(), "v1 has the higher mean");
        assertEquals(0.8, summary.getBestScore(), 1e-9);
        assertEquals("v2", summary.getWorstVersionId());
        assertEquals(0.6, summary.getWorstScore(), 1e-9);
        assertEquals(0.7, summary.getAverageScore(), 1e-9, "mean of 0.8 and 0.6");
    }

    @Test
    void getSummary_emptyWhenNoResults() {
        PromptQualityService service = new PromptQualityService(repoReturning(List.of()),
                new PromptQualityAssembler());
        assertEquals(0, service.getSummary().getTotalVersions());
    }

    private static EvaluationResultEntity result(String version, double score) {
        EvaluationResultEntity e = new EvaluationResultEntity();
        e.setPromptVersion(version);
        e.setScore(score);
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
