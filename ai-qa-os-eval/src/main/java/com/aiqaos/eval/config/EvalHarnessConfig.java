package com.aiqaos.eval.config;

import com.aiqaos.eval.dataset.ClasspathGoldenDatasetProvider;
import com.aiqaos.eval.harness.BaselineStore;
import com.aiqaos.eval.harness.FileBaselineStore;
import com.aiqaos.eval.harness.PromptRegressionHarness;
import com.aiqaos.eval.harness.PromptRunner;
import com.aiqaos.eval.service.PromptEvaluationService;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires AI-3's harness pieces into Spring beans so PE-1's {@code PromptBenchmarkService} is a clean
 * injectable. The baseline directory and regression tolerance are configurable; the golden-dataset
 * source is the classpath provider (chosen concretely to avoid ambiguity with MOD-3's in-memory one).
 */
@Configuration
public class EvalHarnessConfig {

    @Bean
    public BaselineStore baselineStore(@Value("${aiqaos.eval.baseline-dir:golden}") String baselineDir) {
        return new FileBaselineStore(Path.of(baselineDir));
    }

    @Bean
    public PromptRegressionHarness promptRegressionHarness(
            ClasspathGoldenDatasetProvider datasetProvider,
            PromptRunner promptRunner,
            PromptEvaluationService evaluationService,
            BaselineStore baselineStore,
            @Value("${aiqaos.eval.tolerance:0.05}") double tolerance) {
        return new PromptRegressionHarness(
                datasetProvider, promptRunner, evaluationService, baselineStore, tolerance);
    }
}
