package com.aiqaos.eval.evaluator;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.EvaluationResult;
import com.aiqaos.eval.contract.Evaluator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Deterministic evaluator: scores by the fraction of the case's {@code criteria} substrings
 * present in the output. Passes only when every criterion is present.
 */
@Component
public class ContainsEvaluator implements Evaluator {

    @Override
    public String getName() {
        return "contains";
    }

    @Override
    public EvaluationResult evaluate(EvaluationCase testCase, String actualOutput) {
        List<String> criteria = testCase.getCriteria();
        if (criteria.isEmpty()) {
            return new EvaluationResult(getName(), 0.0, false, "no criteria to check");
        }
        String haystack = actualOutput == null ? "" : actualOutput;
        long hits = criteria.stream().filter(c -> c != null && haystack.contains(c)).count();
        double score = (double) hits / criteria.size();
        boolean passed = hits == criteria.size();
        return new EvaluationResult(getName(), score, passed,
                hits + "/" + criteria.size() + " criteria present");
    }
}
