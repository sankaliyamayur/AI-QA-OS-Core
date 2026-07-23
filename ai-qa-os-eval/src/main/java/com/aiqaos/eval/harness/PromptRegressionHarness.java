package com.aiqaos.eval.harness;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.EvaluationReport;
import com.aiqaos.eval.contract.GoldenDatasetProvider;
import com.aiqaos.eval.service.PromptEvaluationService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The AI-3 regression harness: loads a golden suite, produces outputs through a {@link PromptRunner},
 * scores them with MOD-3's {@link PromptEvaluationService}, and compares each case's aggregate
 * score against a committed {@link Baseline}. A case <em>regresses</em> when its score falls more
 * than {@code tolerance} below baseline.
 *
 * <p>This is the engine. Turning {@link RegressionReport#isSuitePassed()} into an enforced CI
 * merge-gate and a formal Prompt Score is PE-1; A/B and dashboards are PE-2/PE-3.
 */
public class PromptRegressionHarness {

    public static final double DEFAULT_TOLERANCE = 0.05;

    private final GoldenDatasetProvider datasetProvider;
    private final PromptRunner promptRunner;
    private final PromptEvaluationService evaluationService;
    private final BaselineStore baselineStore;
    private final double tolerance;

    public PromptRegressionHarness(GoldenDatasetProvider datasetProvider,
                                   PromptRunner promptRunner,
                                   PromptEvaluationService evaluationService,
                                   BaselineStore baselineStore) {
        this(datasetProvider, promptRunner, evaluationService, baselineStore, DEFAULT_TOLERANCE);
    }

    public PromptRegressionHarness(GoldenDatasetProvider datasetProvider,
                                   PromptRunner promptRunner,
                                   PromptEvaluationService evaluationService,
                                   BaselineStore baselineStore,
                                   double tolerance) {
        this.datasetProvider = datasetProvider;
        this.promptRunner = promptRunner;
        this.evaluationService = evaluationService;
        this.baselineStore = baselineStore;
        this.tolerance = tolerance;
    }

    /** Run the suite against its committed baseline and report regressions. */
    public RegressionReport run(String suite, String promptRef) {
        Baseline baseline = baselineStore.load(suite);
        List<RegressionResult> results = new ArrayList<>();
        for (EvaluationCase testCase : datasetProvider.load(suite)) {
            double current = scoreCase(suite, promptRef, testCase);
            Double base = baseline.scoreFor(testCase.getId());
            boolean regressed = base != null && current < base - tolerance;
            results.add(new RegressionResult(testCase.getId(), current, base, regressed));
        }
        return new RegressionReport(suite, results);
    }

    /**
     * Run the suite and write the current scores as the new baseline (the "accept/update" step).
     * Returns a report whose baseline equals the just-recorded current scores (no regressions).
     */
    public RegressionReport updateBaseline(String suite, String promptRef) {
        Map<String, Double> scores = new HashMap<>();
        List<RegressionResult> results = new ArrayList<>();
        for (EvaluationCase testCase : datasetProvider.load(suite)) {
            double current = scoreCase(suite, promptRef, testCase);
            scores.put(testCase.getId(), current);
            results.add(new RegressionResult(testCase.getId(), current, current, false));
        }
        baselineStore.save(new Baseline(suite, scores));
        return new RegressionReport(suite, results);
    }

    private double scoreCase(String suite, String promptRef, EvaluationCase testCase) {
        String output = promptRunner.run(promptRef, testCase);
        EvaluationReport report = evaluationService.evaluate(testCase, output, suite, promptRef);
        return report.aggregateScore();
    }

    public double getTolerance() {
        return tolerance;
    }
}
