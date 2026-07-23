package com.aiqaos.eval.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.EvaluationReport;
import com.aiqaos.eval.contract.Evaluator;
import com.aiqaos.eval.evaluator.ContainsEvaluator;
import com.aiqaos.eval.evaluator.ExactMatchEvaluator;
import com.aiqaos.eval.repository.EvaluationResultRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Unit tests for {@link PromptEvaluationService}. Uses the real deterministic evaluators and a
 * no-op {@link ObjectProvider} for persistence (no JPA context, no Mockito — JDK-25 constraint).
 */
class PromptEvaluationServiceTest {

    /** ObjectProvider that yields no repository, so persistence is skipped. */
    private static ObjectProvider<EvaluationResultRepository> noRepository() {
        return new ObjectProvider<>() {
            @Override
            public EvaluationResultRepository getObject(Object... args) {
                return null;
            }

            @Override
            public EvaluationResultRepository getObject() {
                return null;
            }

            @Override
            public EvaluationResultRepository getIfAvailable() {
                return null;
            }

            @Override
            public EvaluationResultRepository getIfUnique() {
                return null;
            }
        };
    }

    private static PromptEvaluationService service(List<Evaluator> evaluators) {
        return new PromptEvaluationService(evaluators, noRepository());
    }

    @Test
    void evaluate_runsAllEvaluatorsAndAggregates() {
        PromptEvaluationService svc = service(List.of(new ExactMatchEvaluator(), new ContainsEvaluator()));
        EvaluationCase testCase = new EvaluationCase("c1", "in", "hello", List.of("hello"), List.of());

        EvaluationReport report = svc.evaluate(testCase, "hello", "suite-a", "v1");

        assertThat(report.getResults()).hasSize(2);
        assertThat(report.passed()).isTrue();
        assertThat(report.aggregateScore()).isEqualTo(1.0);
    }

    @Test
    void evaluate_recordsFailureWhenAnEvaluatorThrows() {
        Evaluator boom = new Evaluator() {
            @Override
            public String getName() {
                return "boom";
            }

            @Override
            public com.aiqaos.eval.contract.EvaluationResult evaluate(EvaluationCase t, String o) {
                throw new RuntimeException("kaboom");
            }
        };
        PromptEvaluationService svc = service(List.of(boom));
        EvaluationReport report = svc.evaluate(
                new EvaluationCase("c1", "in", "x", List.of(), List.of()), "x", "suite-a", "v1");

        assertThat(report.getResults()).hasSize(1);
        assertThat(report.passed()).isFalse();
        assertThat(report.getResults().get(0).getReason()).contains("evaluator error");
    }

    @Test
    void evaluateSuite_pairsOutputsByCaseId() {
        PromptEvaluationService svc = service(List.of(new ExactMatchEvaluator()));
        List<EvaluationCase> cases = List.of(
                new EvaluationCase("a", "in", "1", List.of(), List.of()),
                new EvaluationCase("b", "in", "2", List.of(), List.of()));

        List<EvaluationReport> reports =
                svc.evaluateSuite("suite-a", cases, Map.of("a", "1", "b", "WRONG"), "v1");

        assertThat(reports).hasSize(2);
        assertThat(reports.get(0).passed()).isTrue();
        assertThat(reports.get(1).passed()).isFalse();
    }
}
