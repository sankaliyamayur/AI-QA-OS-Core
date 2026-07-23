package com.aiqaos.eval.contract;

import java.util.List;

/**
 * A golden case: an input, an optional expected output, and pass criteria. Evaluators
 * interpret {@code criteria} in their own way (substrings for {@code contains}, required
 * JSON field names for {@code json-validity}, rubric points for the LLM judge).
 *
 * <p>MOD-3 supplies these via an in-memory provider; PE-1 will manage golden datasets as
 * a first-class, durable/vector-backed concept behind {@link GoldenDatasetProvider}.
 */
public class EvaluationCase {

    private final String id;
    private final String input;
    private final String expectedOutput;
    private final List<String> criteria;
    private final List<String> tags;

    public EvaluationCase(String id, String input, String expectedOutput,
                          List<String> criteria, List<String> tags) {
        this.id = id;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.criteria = criteria == null ? List.of() : List.copyOf(criteria);
        this.tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public String getId() {
        return id;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public List<String> getCriteria() {
        return criteria;
    }

    public List<String> getTags() {
        return tags;
    }
}
