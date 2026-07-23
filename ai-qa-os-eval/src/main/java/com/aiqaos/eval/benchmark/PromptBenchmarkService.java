package com.aiqaos.eval.benchmark;

import com.aiqaos.eval.harness.PromptRegressionHarness;
import com.aiqaos.eval.harness.RegressionReport;
import com.aiqaos.eval.harness.RegressionResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * PE-1's productisation of AI-3's harness: computes a version-level {@link PromptScore} (the
 * <em>Prompt Benchmark</em>) and a regression {@link BenchmarkVerdict} (the <em>Prompt Regression
 * Test</em>) across one or more golden suites. Per-evaluator results are persisted by MOD-3's
 * {@code PromptEvaluationService} during each harness run, so PE-1 adds no new table.
 */
@Service
public class PromptBenchmarkService {

    private final PromptRegressionHarness harness;

    public PromptBenchmarkService(PromptRegressionHarness harness) {
        this.harness = harness;
    }

    /** Benchmark a prompt version across suites → an overall {@link PromptScore}. */
    public PromptScore benchmark(String promptRef, List<String> suites) {
        Map<String, Double> perSuite = new LinkedHashMap<>();
        List<Double> allCaseScores = new ArrayList<>();
        for (String suite : suites) {
            RegressionReport report = harness.run(suite, promptRef);
            double suiteMean = report.getResults().stream()
                    .mapToDouble(RegressionResult::getCurrentScore)
                    .average()
                    .orElse(0.0);
            perSuite.put(suite, suiteMean);
            report.getResults().forEach(r -> allCaseScores.add(r.getCurrentScore()));
        }
        double overall = allCaseScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return new PromptScore(promptRef, overall, perSuite, allCaseScores.size());
    }

    /** Regression-test a prompt version: fail if any suite regressed against its committed baseline. */
    public BenchmarkVerdict checkRegression(String promptRef, List<String> suites) {
        List<String> regressed = new ArrayList<>();
        for (String suite : suites) {
            if (!harness.run(suite, promptRef).isSuitePassed()) {
                regressed.add(suite);
            }
        }
        return new BenchmarkVerdict(regressed.isEmpty(), regressed);
    }
}
