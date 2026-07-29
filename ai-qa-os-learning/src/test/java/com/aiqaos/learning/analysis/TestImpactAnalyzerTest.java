package com.aiqaos.learning.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestImpactAnalyzerTest {

    private TestImpactAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new TestImpactAnalyzer();
    }

    @Test
    void testAnalyzeImpactedStepsForLoginDomain() {
        List<String> modifiedFiles = List.of("src/main/java/com/example/auth/LoginController.java");
        List<String> allSteps = List.of("TEST_CASE_GENERATOR", "SCRIPT_GENERATOR", "EXECUTION_ENGINEER", "REPORTING", "SELF_HEALING");

        List<String> impacted = analyzer.analyzeImpactedSteps(modifiedFiles, allSteps);

        assertEquals(3, impacted.size());
        assertTrue(impacted.contains("TEST_CASE_GENERATOR"));
        assertTrue(impacted.contains("SCRIPT_GENERATOR"));
        assertTrue(impacted.contains("EXECUTION_ENGINEER"));
        assertFalse(impacted.contains("REPORTING"));
    }

    @Test
    void testFallbackToAllStepsWhenUnmappedFile() {
        List<String> modifiedFiles = List.of("README.md");
        List<String> allSteps = List.of("TEST_CASE_GENERATOR", "REPORTING");

        List<String> impacted = analyzer.analyzeImpactedSteps(modifiedFiles, allSteps);

        assertEquals(2, impacted.size());
        assertTrue(impacted.containsAll(allSteps));
    }
}
