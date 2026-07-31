package com.aiqaos.dashboard.service;

import com.aiqaos.eval.benchmark.LeaderboardEntry;
import com.aiqaos.eval.benchmark.PromptQualityAssembler;
import com.aiqaos.eval.benchmark.PromptQualitySummary;
import com.aiqaos.eval.entity.EvaluationResultEntity;
import com.aiqaos.eval.repository.EvaluationResultRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * PE-3 (ADR-062): serves the prompt-quality read-model by AGGREGATING persisted evaluation results
 * (eval_results) into a version leaderboard — the mean score per prompt version, ranked — then
 * delegating to the pure {@link PromptQualityAssembler}. Reads existing data; no benchmark is re-run.
 */
@Service
public class PromptQualityService {

    private final EvaluationResultRepository evaluationResultRepository;
    private final PromptQualityAssembler assembler;

    public PromptQualityService(EvaluationResultRepository evaluationResultRepository,
                                PromptQualityAssembler assembler) {
        this.evaluationResultRepository = evaluationResultRepository;
        this.assembler = assembler;
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
}
