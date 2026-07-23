package com.aiqaos.eval.contract;

/**
 * An evaluator scores an actual output against a golden {@link EvaluationCase}.
 *
 * <p>MOD-3 ships deterministic reference evaluators and a minimal LLM-judge; AI-3 (the
 * regression harness) and PE-1 (scoring/benchmarking) add more implementations of this
 * same contract.
 */
public interface Evaluator {

    /** Stable, human-readable evaluator name (e.g. {@code "exact-match"}). */
    String getName();

    /**
     * Score {@code actualOutput} against the golden {@code testCase}.
     *
     * @return an {@link EvaluationResult}; implementations must not throw for normal
     *         inputs — return a failed result with a reason instead.
     */
    EvaluationResult evaluate(EvaluationCase testCase, String actualOutput);
}
