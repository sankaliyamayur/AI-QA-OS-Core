package com.aiqaos.eval.evaluator;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.EvaluationResult;
import com.aiqaos.eval.contract.Evaluator;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Structural evaluator: the output must parse as JSON, and (if the case lists {@code criteria})
 * must contain each named top-level field. Directly useful — the pipeline's agents emit JSON.
 */
@Component
public class JsonValidityEvaluator implements Evaluator {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getName() {
        return "json-validity";
    }

    @Override
    public EvaluationResult evaluate(EvaluationCase testCase, String actualOutput) {
        if (actualOutput == null || actualOutput.isBlank()) {
            return new EvaluationResult(getName(), 0.0, false, "empty output");
        }
        JsonNode node;
        try {
            node = mapper.readTree(actualOutput);
        } catch (Exception e) {
            return new EvaluationResult(getName(), 0.0, false, "invalid JSON: " + e.getMessage());
        }
        List<String> required = testCase.getCriteria();
        if (required.isEmpty()) {
            return new EvaluationResult(getName(), 1.0, true, "valid JSON");
        }
        long present = required.stream().filter(node::has).count();
        double score = (double) present / required.size();
        boolean passed = present == required.size();
        return new EvaluationResult(getName(), score, passed,
                present + "/" + required.size() + " required fields present");
    }
}
