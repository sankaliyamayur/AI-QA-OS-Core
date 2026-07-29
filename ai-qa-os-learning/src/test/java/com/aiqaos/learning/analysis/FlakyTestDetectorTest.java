package com.aiqaos.learning.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlakyTestDetectorTest {

    private FlakyTestDetector detector;

    @BeforeEach
    void setUp() {
        detector = new FlakyTestDetector(0.30);
    }

    @Test
    void testStablePassingTest() {
        List<String> outcomes = List.of("PASSED", "PASSED", "PASSED", "PASSED");
        FlakyTestReport report = detector.analyzeStepHistory("EXECUTION_ENGINEER", outcomes);

        assertEquals("EXECUTION_ENGINEER", report.getStepName());
        assertEquals(4, report.getTotalExecutions());
        assertEquals(0, report.getFailureCount());
        assertEquals(0, report.getFlipCount());
        assertEquals(FlakyTestReport.Recommendation.STABLE, report.getRecommendation());
    }

    @Test
    void testFlakyTestDetection() {
        // High oscillation: PASSED -> FAILED -> PASSED -> FAILED
        List<String> outcomes = List.of("PASSED", "FAILED", "PASSED", "FAILED", "PASSED");
        FlakyTestReport report = detector.analyzeStepHistory("SCRIPT_GENERATOR", outcomes);

        assertEquals(5, report.getTotalExecutions());
        assertEquals(2, report.getFailureCount());
        assertEquals(4, report.getFlipCount());
        assertTrue(report.getFlakinessScore() >= 0.30);
        assertNotEquals(FlakyTestReport.Recommendation.STABLE, report.getRecommendation());
    }

    @Test
    void testEmptyHistory() {
        FlakyTestReport report = detector.analyzeStepHistory("UNKNOWN_STEP", List.of());
        assertEquals(0, report.getTotalExecutions());
        assertEquals(FlakyTestReport.Recommendation.STABLE, report.getRecommendation());
    }
}
