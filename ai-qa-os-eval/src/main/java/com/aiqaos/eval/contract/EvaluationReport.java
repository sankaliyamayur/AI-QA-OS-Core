package com.aiqaos.eval.contract;

import java.util.List;

/**
 * All {@link EvaluationResult}s produced for a single {@link EvaluationCase}, plus simple
 * aggregates. AI-3/PE-1 aggregate these further across a whole suite.
 */
public class EvaluationReport {

    private final String caseId;
    private final List<EvaluationResult> results;

    public EvaluationReport(String caseId, List<EvaluationResult> results) {
        this.caseId = caseId;
        this.results = results == null ? List.of() : List.copyOf(results);
    }

    /** Mean score across all evaluators (0 when there are no results). */
    public double aggregateScore() {
        return results.stream().mapToDouble(EvaluationResult::getScore).average().orElse(0.0);
    }

    /** True only when there is at least one result and every evaluator passed. */
    public boolean passed() {
        return !results.isEmpty() && results.stream().allMatch(EvaluationResult::isPassed);
    }

    public String getCaseId() {
        return caseId;
    }

    public List<EvaluationResult> getResults() {
        return results;
    }
}
