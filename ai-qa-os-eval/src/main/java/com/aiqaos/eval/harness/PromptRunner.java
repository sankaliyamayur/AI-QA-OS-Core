package com.aiqaos.eval.harness;

import com.aiqaos.eval.contract.EvaluationCase;

/**
 * Produces the actual output for a golden case under a named prompt version. The seam that
 * lets the harness stay runner-agnostic: production runs the real prompt+LLM
 * ({@link LlmPromptRunner}); tests pass a deterministic stub.
 */
public interface PromptRunner {

    /**
     * @param promptRef identifies the prompt version to exercise (e.g. a template name)
     * @param testCase  the golden case supplying the input
     * @return the produced output to be evaluated
     */
    String run(String promptRef, EvaluationCase testCase);
}
