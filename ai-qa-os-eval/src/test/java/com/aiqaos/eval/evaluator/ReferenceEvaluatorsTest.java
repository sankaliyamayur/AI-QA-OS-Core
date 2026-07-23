package com.aiqaos.eval.evaluator;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.EvaluationResult;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for the deterministic reference evaluators. */
class ReferenceEvaluatorsTest {

    private static EvaluationCase caseWith(String expected, List<String> criteria) {
        return new EvaluationCase("c1", "input", expected, criteria, List.of());
    }

    @Test
    void exactMatch_passesOnEqualOutput() {
        EvaluationResult r = new ExactMatchEvaluator().evaluate(caseWith("hello", List.of()), "hello");
        assertThat(r.isPassed()).isTrue();
        assertThat(r.getScore()).isEqualTo(1.0);
    }

    @Test
    void exactMatch_failsOnDifferentOutput() {
        EvaluationResult r = new ExactMatchEvaluator().evaluate(caseWith("hello", List.of()), "world");
        assertThat(r.isPassed()).isFalse();
        assertThat(r.getScore()).isEqualTo(0.0);
    }

    @Test
    void contains_scoresByFractionOfCriteriaPresent() {
        EvaluationResult r = new ContainsEvaluator()
                .evaluate(caseWith(null, List.of("alpha", "beta")), "only alpha here");
        assertThat(r.getScore()).isEqualTo(0.5);
        assertThat(r.isPassed()).isFalse();
    }

    @Test
    void contains_passesWhenAllPresent() {
        EvaluationResult r = new ContainsEvaluator()
                .evaluate(caseWith(null, List.of("alpha", "beta")), "alpha and beta");
        assertThat(r.getScore()).isEqualTo(1.0);
        assertThat(r.isPassed()).isTrue();
    }

    @Test
    void jsonValidity_failsOnInvalidJson() {
        EvaluationResult r = new JsonValidityEvaluator().evaluate(caseWith(null, List.of()), "not json");
        assertThat(r.isPassed()).isFalse();
        assertThat(r.getScore()).isEqualTo(0.0);
    }

    @Test
    void jsonValidity_passesOnValidJsonWithRequiredFields() {
        EvaluationResult r = new JsonValidityEvaluator()
                .evaluate(caseWith(null, List.of("name", "status")), "{\"name\":\"x\",\"status\":\"ok\"}");
        assertThat(r.isPassed()).isTrue();
        assertThat(r.getScore()).isEqualTo(1.0);
    }

    @Test
    void jsonValidity_partialScoreWhenFieldMissing() {
        EvaluationResult r = new JsonValidityEvaluator()
                .evaluate(caseWith(null, List.of("name", "status")), "{\"name\":\"x\"}");
        assertThat(r.getScore()).isEqualTo(0.5);
        assertThat(r.isPassed()).isFalse();
    }
}
