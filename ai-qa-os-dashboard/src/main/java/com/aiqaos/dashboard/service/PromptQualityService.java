package com.aiqaos.dashboard.service;

import com.aiqaos.eval.benchmark.LeaderboardEntry;
import com.aiqaos.eval.benchmark.PromptQualityAssembler;
import com.aiqaos.eval.benchmark.PromptQualitySummary;
import com.aiqaos.eval.benchmark.PromptRegressionAnalyzer;
import com.aiqaos.eval.benchmark.PromptRegressionReport;
import com.aiqaos.eval.entity.EvaluationResultEntity;
import com.aiqaos.eval.repository.EvaluationResultRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * PE-3 (ADR-062): serves the prompt-quality read-model by AGGREGATING persisted evaluation results
 * (eval_results) into a version leaderboard — the mean score per prompt version, ranked — then
 * delegating to the pure {@link PromptQualityAssembler}. Reads existing data; no benchmark is re-run.
 *
 * <p>FI-PE3-B (ADR-069): also serves the regression read-model — versions whose recent scores dropped
 * below their earlier scores — via {@link PromptRegressionAnalyzer} over the same {@code eval_results}.
 */
@Service
public class PromptQualityService {

    private final EvaluationResultRepository evaluationResultRepository;
    private final PromptQualityAssembler assembler;
    private final PromptRegressionAnalyzer regressionAnalyzer;
    private final double regressionTolerance;
    private final int regressionMinSamples;

    public PromptQualityService(EvaluationResultRepository evaluationResultRepository,
                                PromptQualityAssembler assembler,
                                PromptRegressionAnalyzer regressionAnalyzer,
                                @Value("${aiqaos.eval.regression.tolerance:0.05}") double regressionTolerance,
                                @Value("${aiqaos.eval.regression.min-samples:4}") int regressionMinSamples) {
        this.evaluationResultRepository = evaluationResultRepository;
        this.assembler = assembler;
        this.regressionAnalyzer = regressionAnalyzer;
        this.regressionTolerance = regressionTolerance;
        this.regressionMinSamples = regressionMinSamples;
    }

    public PromptQualitySummary getSummary() {
        Map<String, Double> meanScoreByVersion = evaluationResultRepository.findAll().stream()
                .filter(r -> r.getPromptVersion() != null && !r.getPromptVersion().isBlank())
                .collect(Collectors.groupingBy(
                        EvaluationResultEntity::getPromptVersion,
                        Collectors.averagingDouble(EvaluationResultEntity::getScore)));

        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<String, Double> e : meanScoreByVersion.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .toList()) {
            leaderboard.add(new LeaderboardEntry(e.getKey(), e.getValue(), rank++));
        }
        return assembler.summarize(leaderboard);
    }

    /**
     * FI-PE3-B (ADR-069): the regression read-model — versions whose recent evaluation scores dropped
     * below their earlier scores. Groups {@code eval_results} by version, orders each version's scores
     * by {@code createdTime} (nulls last, so undated rows never masquerade as "recent"), and delegates
     * to the pure analyzer. No benchmark is re-run; versions with too few results are skipped.
     */
    public PromptRegressionReport getRegressions() {
        Map<String, List<EvaluationResultEntity>> byVersion = evaluationResultRepository.findAll().stream()
                .filter(r -> r.getPromptVersion() != null && !r.getPromptVersion().isBlank())
                .collect(Collectors.groupingBy(EvaluationResultEntity::getPromptVersion));

        Map<String, List<Double>> chronologicalScoresByVersion = new LinkedHashMap<>();
        for (Map.Entry<String, List<EvaluationResultEntity>> e : byVersion.entrySet()) {
            List<Double> scores = e.getValue().stream()
                    .sorted(Comparator.comparing(EvaluationResultEntity::getCreatedTime,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(EvaluationResultEntity::getScore)
                    .collect(Collectors.toList());
            chronologicalScoresByVersion.put(e.getKey(), scores);
        }
        return regressionAnalyzer.analyze(chronologicalScoresByVersion, regressionTolerance, regressionMinSamples);
    }
}
