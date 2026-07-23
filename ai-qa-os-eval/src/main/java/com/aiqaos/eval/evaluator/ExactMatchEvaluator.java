package com.aiqaos.eval.evaluator;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.EvaluationResult;
import com.aiqaos.eval.contract.Evaluator;
import org.springframework.stereotype.Component;

/**
 * Deterministic evaluator: the output must exactly equal the case's expected output.
 */
@Component
public class ExactMatchEvaluator implements Evaluator {

    @Override
    public String getName() {
        return "exact-match";
    }

    @Override
    public EvaluationResult evaluate(EvaluationCase testCase, String actualOutput) {
        String expected = testCase.getExpectedOutput();
        boolean match = expected != null && expected.equals(actualOutput);
        return new EvaluationResult(getName(), match ? 1.0 : 0.0, match,
                match ? "exact match" : "output does not equal expected");
    }
}
