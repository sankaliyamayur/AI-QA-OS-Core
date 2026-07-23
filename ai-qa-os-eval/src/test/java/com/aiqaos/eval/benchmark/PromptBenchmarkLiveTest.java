package com.aiqaos.eval.benchmark;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The real prompt-quality regression gate: runs the benchmark against the committed baselines using
 * the live LLM. Gated by {@code AIQAOS_PROMPT_EVAL_LIVE=true} so it is <b>skipped</b> in ordinary
 * (key-less) CI and only runs in the key-gated {@code deploy.yml} step where a provider is configured.
 *
 * <p>Named {@code *Test} (not {@code *IT}) so Surefire collects it; the env condition disables it
 * before any Spring context loads. Its enabled path requires a provider-keyed environment.
 */
@SpringBootTest(classes = EvalTestApplication.class)
@EnabledIfEnvironmentVariable(named = "AIQAOS_PROMPT_EVAL_LIVE", matches = "true")
class PromptBenchmarkLiveTest {

    @Autowired
    private PromptBenchmarkService benchmarkService;

    @Value("${aiqaos.eval.live.prompt-ref:default-agent}")
    private String promptRef;

    @Value("${aiqaos.eval.live.suites:sample-suite}")
    private String suitesCsv;

    @Test
    void liveBenchmarkHasNoRegression() {
        List<String> suites = Arrays.stream(suitesCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        BenchmarkVerdict verdict = benchmarkService.checkRegression(promptRef, suites);

        assertThat(verdict.isPassed())
                .as("prompt '%s' regressed on suites %s", promptRef, verdict.getRegressedSuites())
                .isTrue();
    }
}
