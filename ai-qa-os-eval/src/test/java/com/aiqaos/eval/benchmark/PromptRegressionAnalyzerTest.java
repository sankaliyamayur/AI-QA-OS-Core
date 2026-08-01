package com.aiqaos.eval.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * FI-PE3-B (ADR-069): the pure regression analyzer. "Regression" = a version's recent (chronological
 * second-half) mean falling below its earlier (first-half) mean by more than the tolerance.
 */
class PromptRegressionAnalyzerTest {

    private final PromptRegressionAnalyzer analyzer = new PromptRegressionAnalyzer();

    @Test
    void flagsVersionThatDeclinedOverTime() {
        // earlier mean = 0.9, recent mean = 0.5 -> delta -0.4, well past tolerance
        Map<String, List<Double>> in = Map.of("v1", List.of(0.9, 0.9, 0.5, 0.5));
        PromptRegressionReport report = analyzer.analyze(in, 0.05, 4);

        assertEquals(1, report.regressedCount());
        PromptRegressionSignal s = report.regressions().get(0);
        assertEquals("v1", s.versionId());
        assertEquals(0.9, s.baselineScore(), 1e-9);
        assertEquals(0.5, s.currentScore(), 1e-9);
        assertTrue(s.delta() < 0);
        assertEquals(4, s.sampleCount());
    }

    @Test
    void doesNotFlagStableVersion() {
        Map<String, List<Double>> in = Map.of("v1", List.of(0.8, 0.8, 0.79, 0.81));
        assertEquals(0, analyzer.analyze(in, 0.05, 4).regressedCount());
    }

    @Test
    void doesNotFlagImprovement() {
        Map<String, List<Double>> in = Map.of("v1", List.of(0.5, 0.5, 0.9, 0.9));
        assertEquals(0, analyzer.analyze(in, 0.05, 4).regressedCount());
    }

    @Test
    void toleranceBoundary_dropEqualToToleranceNotFlagged() {
        // earlier 0.80, recent 0.75 -> delta exactly -0.05; flagged only if strictly beyond tolerance
        Map<String, List<Double>> in = Map.of("v1", List.of(0.80, 0.80, 0.75, 0.75));
        assertEquals(0, analyzer.analyze(in, 0.05, 4).regressedCount());
    }

    @Test
    void skipsVersionWithInsufficientSamples() {
        Map<String, List<Double>> in = Map.of("v1", List.of(0.9, 0.2)); // 2 < minSamples 4
        assertEquals(0, analyzer.analyze(in, 0.05, 4).regressedCount(), "too few results -> skipped, not fabricated");
    }

    @Test
    void ranksMultipleRegressionsWorstFirst() {
        Map<String, List<Double>> in = new LinkedHashMap<>();
        in.put("small", List.of(0.8, 0.8, 0.6, 0.6));  // delta -0.2
        in.put("big", List.of(0.9, 0.9, 0.3, 0.3));    // delta -0.6
        PromptRegressionReport report = analyzer.analyze(in, 0.05, 4);

        assertEquals(2, report.regressedCount());
        assertEquals("big", report.regressions().get(0).versionId(), "worst (most negative delta) first");
        assertEquals("small", report.regressions().get(1).versionId());
    }

    @Test
    void emptyInputYieldsEmptyReport() {
        assertEquals(0, analyzer.analyze(Map.of(), 0.05, 4).regressedCount());
        assertEquals(0, analyzer.analyze(null, 0.05, 4).regressedCount());
    }
}
