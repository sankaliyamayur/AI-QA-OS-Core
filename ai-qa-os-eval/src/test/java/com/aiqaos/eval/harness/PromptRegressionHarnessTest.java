package com.aiqaos.eval.harness;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.Evaluator;
import com.aiqaos.eval.contract.GoldenDatasetProvider;
import com.aiqaos.eval.dataset.ClasspathGoldenDatasetProvider;
import com.aiqaos.eval.evaluator.ContainsEvaluator;
import com.aiqaos.eval.repository.EvaluationResultRepository;
import com.aiqaos.eval.service.PromptEvaluationService;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Tests the regression harness end-to-end with a stub {@link PromptRunner}, the real
 * {@link ContainsEvaluator}, and a file baseline in a temp dir. No live LLM, no Mockito.
 */
class PromptRegressionHarnessTest {

    /** A single case whose "good" criterion drives a deterministic contains-score. */
    private static GoldenDatasetProvider oneCaseSuite() {
        EvaluationCase c = new EvaluationCase("c1", "input", null, List.of("good"), List.of());
        return suite -> List.of(c);
    }

    private static PromptEvaluationService evaluationService() {
        return new PromptEvaluationService(List.<Evaluator>of(new ContainsEvaluator()), noRepository());
    }

    @Test
    void run_flagsRegressionWhenScoreDropsBelowBaseline(@TempDir Path dir) {
        FileBaselineStore store = new FileBaselineStore(dir);
        store.save(new Baseline("s", new HashMap<>(Map.of("c1", 1.0))));

        // Runner returns output WITHOUT "good" -> contains score 0.0 -> regressed vs 1.0.
        PromptRunner runner = (ref, testCase) -> "nothing here";
        PromptRegressionHarness harness =
                new PromptRegressionHarness(oneCaseSuite(), runner, evaluationService(), store);

        RegressionReport report = harness.run("s", "prompt-v1");

        assertThat(report.getResults()).hasSize(1);
        assertThat(report.getResults().get(0).getCurrentScore()).isEqualTo(0.0);
        assertThat(report.getResults().get(0).isRegressed()).isTrue();
        assertThat(report.isSuitePassed()).isFalse();
        assertThat(report.regressionCount()).isEqualTo(1);
    }

    @Test
    void run_passesWhenScoreMeetsBaseline(@TempDir Path dir) {
        FileBaselineStore store = new FileBaselineStore(dir);
        store.save(new Baseline("s", new HashMap<>(Map.of("c1", 1.0))));

        PromptRunner runner = (ref, testCase) -> "this is good";
        PromptRegressionHarness harness =
                new PromptRegressionHarness(oneCaseSuite(), runner, evaluationService(), store);

        RegressionReport report = harness.run("s", "prompt-v1");

        assertThat(report.getResults().get(0).getCurrentScore()).isEqualTo(1.0);
        assertThat(report.getResults().get(0).isRegressed()).isFalse();
        assertThat(report.isSuitePassed()).isTrue();
    }

    @Test
    void run_noBaselineMeansNoRegression(@TempDir Path dir) {
        FileBaselineStore store = new FileBaselineStore(dir); // empty dir -> empty baseline
        PromptRunner runner = (ref, testCase) -> "nothing";
        PromptRegressionHarness harness =
                new PromptRegressionHarness(oneCaseSuite(), runner, evaluationService(), store);

        RegressionReport report = harness.run("s", "prompt-v1");

        assertThat(report.getResults().get(0).getBaselineScore()).isNull();
        assertThat(report.isSuitePassed()).isTrue();
    }

    @Test
    void updateBaseline_writesCurrentScores(@TempDir Path dir) {
        FileBaselineStore store = new FileBaselineStore(dir);
        PromptRunner runner = (ref, testCase) -> "this is good";
        PromptRegressionHarness harness =
                new PromptRegressionHarness(oneCaseSuite(), runner, evaluationService(), store);

        harness.updateBaseline("s", "prompt-v1");

        Baseline reloaded = store.load("s");
        assertThat(reloaded.scoreFor("c1")).isEqualTo(1.0);
    }

    @Test
    void classpathProvider_loadsSampleSuite() {
        List<EvaluationCase> cases = new ClasspathGoldenDatasetProvider().load("sample-suite");
        assertThat(cases).hasSize(2);
        assertThat(cases.get(0).getId()).isEqualTo("greeting");
        assertThat(cases.get(1).getCriteria()).containsExactly("name", "status");
    }

    /** ObjectProvider that yields no repository (persistence skipped). */
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
}
