package com.aiqaos.eval.service;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.EvaluationReport;
import com.aiqaos.eval.contract.EvaluationResult;
import com.aiqaos.eval.contract.Evaluator;
import com.aiqaos.eval.entity.EvaluationResultEntity;
import com.aiqaos.eval.repository.EvaluationResultRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Runs the registered {@link Evaluator}s over golden cases, aggregates the results, and
 * (best-effort) persists them. This is the entry point AI-3's regression harness and PE-1's
 * benchmarking build on; MOD-3 provides the mechanism, not the full harness.
 */
@Service
public class PromptEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(PromptEvaluationService.class);

    private final List<Evaluator> evaluators;
    private final ObjectProvider<EvaluationResultRepository> repositoryProvider;

    public PromptEvaluationService(List<Evaluator> evaluators,
                                   ObjectProvider<EvaluationResultRepository> repositoryProvider) {
        this.evaluators = evaluators;
        this.repositoryProvider = repositoryProvider;
    }

    /**
     * Evaluate one case's actual output with every registered evaluator. An evaluator that
     * throws is recorded as a failed result rather than aborting the run.
     */
    public EvaluationReport evaluate(EvaluationCase testCase, String actualOutput,
                                     String suite, String promptVersion) {
        List<EvaluationResult> results = new ArrayList<>();
        for (Evaluator evaluator : evaluators) {
            EvaluationResult result;
            try {
                result = evaluator.evaluate(testCase, actualOutput);
            } catch (Exception e) {
                result = new EvaluationResult(evaluator.getName(), 0.0, false,
                        "evaluator error: " + e.getMessage());
            }
            results.add(result);
            persist(suite, testCase.getId(), promptVersion, result);
        }
        return new EvaluationReport(testCase.getId(), results);
    }

    /**
     * Evaluate a whole suite: each case is paired with its actual output by case id (missing
     * outputs are treated as {@code null}).
     */
    public List<EvaluationReport> evaluateSuite(String suite, List<EvaluationCase> cases,
                                                Map<String, String> outputsByCaseId,
                                                String promptVersion) {
        List<EvaluationReport> reports = new ArrayList<>();
        for (EvaluationCase testCase : cases) {
            String output = outputsByCaseId == null ? null : outputsByCaseId.get(testCase.getId());
            reports.add(evaluate(testCase, output, suite, promptVersion));
        }
        return reports;
    }

    private void persist(String suite, String caseId, String promptVersion, EvaluationResult result) {
        EvaluationResultRepository repository = repositoryProvider.getIfAvailable();
        if (repository == null) {
            return;
        }
        try {
            EvaluationResultEntity entity = new EvaluationResultEntity();
            entity.setResultId(UUID.randomUUID());
            entity.setSuite(suite);
            entity.setCaseId(caseId);
            entity.setEvaluator(result.getEvaluatorName());
            entity.setScore(result.getScore());
            entity.setPassed(result.isPassed());
            entity.setPromptVersion(promptVersion);
            entity.setReason(result.getReason());
            entity.setCreatedTime(LocalDateTime.now());
            repository.save(entity);
        } catch (Exception e) {
            log.warn("Failed to persist evaluation result for case {} ({}): {}",
                    caseId, result.getEvaluatorName(), e.getMessage());
        }
    }
}
