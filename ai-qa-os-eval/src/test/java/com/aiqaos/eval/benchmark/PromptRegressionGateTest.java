package com.aiqaos.eval.benchmark;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.Evaluator;
import com.aiqaos.eval.dataset.ClasspathGoldenDatasetProvider;
import com.aiqaos.eval.evaluator.ContainsEvaluator;
import com.aiqaos.eval.evaluator.JsonValidityEvaluator;
import com.aiqaos.eval.harness.Baseline;
import com.aiqaos.eval.harness.BaselineStore;
import com.aiqaos.eval.harness.FileBaselineStore;
import com.aiqaos.eval.harness.PromptRegressionHarness;
import com.aiqaos.eval.harness.PromptRunner;
import com.aiqaos.eval.repository.EvaluationResultRepository;
import com.aiqaos.eval.service.PromptEvaluationService;
import tools.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * PE-1's always-on regression gate (Option A). Runs in every {@code mvn verify} with no LLM or key:
 * asserts (1) every committed golden suite has a baseline covering all its case ids, and (2) the
 * benchmark → score → regression pipeline runs over a deterministic stub runner. Real prompt-quality
 * scoring runs in the key-gated {@code PromptBenchmarkLiveTest}.
 */
class PromptRegressionGateTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void everyGoldenSuiteHasACoveringBaseline() throws Exception {
        ClasspathGoldenDatasetProvider provider = new ClasspathGoldenDatasetProvider();
        List<String> suites = discoverSuites();
        assertThat(suites).as("at least one committed golden suite").isNotEmpty();

        for (String suite : suites) {
            List<EvaluationCase> cases = provider.load(suite);
            assertThat(cases).as("suite '%s' has cases", suite).isNotEmpty();

            try (InputStream in = getClass().getClassLoader()
                    .getResourceAsStream("golden/" + suite + ".baseline.json")) {
                assertThat(in).as("suite '%s' has a committed baseline file", suite).isNotNull();
                Baseline baseline = mapper.readValue(in, Baseline.class);
                for (EvaluationCase c : cases) {
                    assertThat(baseline.scoreFor(c.getId()))
                            .as("baseline for suite '%s' covers case '%s'", suite, c.getId())
                            .isNotNull();
                }
            }
        }
    }

    @Test
    void benchmarkPipelineRunsOverGoldenData() throws Exception {
        Path goldenDir = Path.of(getClass().getClassLoader().getResource("golden").toURI());
        BaselineStore baselineStore = new FileBaselineStore(goldenDir);
        PromptEvaluationService evalService = new PromptEvaluationService(
                List.<Evaluator>of(new ContainsEvaluator(), new JsonValidityEvaluator()), noRepository());
        // Deterministic runner: echoes the case criteria so scores are stable (no LLM).
        PromptRunner stubRunner = (ref, testCase) -> "Hello " + String.join(" ", testCase.getCriteria());
        PromptRegressionHarness harness = new PromptRegressionHarness(
                new ClasspathGoldenDatasetProvider(), stubRunner, evalService, baselineStore);
        PromptBenchmarkService service = new PromptBenchmarkService(harness);

        PromptScore score = service.benchmark("prompt-under-test", List.of("sample-suite"));
        assertThat(score.getCaseCount()).isEqualTo(2);
        assertThat(score.getOverall()).isBetween(0.0, 1.0);
        assertThat(score.getPerSuite()).containsKey("sample-suite");

        BenchmarkVerdict verdict = service.checkRegression("prompt-under-test", List.of("sample-suite"));
        assertThat(verdict).isNotNull();
        assertThat(verdict.getRegressedSuites()).isSubsetOf("sample-suite");
    }

    private static List<String> discoverSuites() throws Exception {
        URL url = PromptRegressionGateTest.class.getClassLoader().getResource("golden");
        assertThat(url).as("golden/ resource directory exists").isNotNull();
        Path dir = Path.of(url.toURI());
        try (Stream<Path> files = Files.list(dir)) {
            return files.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".json") && !n.endsWith(".baseline.json"))
                    .map(n -> n.substring(0, n.length() - ".json".length()))
                    .sorted()
                    .toList();
        }
    }

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
