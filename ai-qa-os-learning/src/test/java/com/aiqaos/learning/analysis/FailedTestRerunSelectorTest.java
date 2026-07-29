package com.aiqaos.learning.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FailedTestRerunSelectorTest {

    private FailedTestRerunSelector selector;

    @BeforeEach
    void setUp() {
        selector = new FailedTestRerunSelector();
    }

    @Test
    void testSelectFailedStepsOnly() {
        List<FailedTestRerunSelector.StepExecutionRecord> records = List.of(
                new FailedTestRerunSelector.StepExecutionRecord("STEP_1", "PASSED"),
                new FailedTestRerunSelector.StepExecutionRecord("STEP_2", "FAILED"),
                new FailedTestRerunSelector.StepExecutionRecord("STEP_3", "PASSED"),
                new FailedTestRerunSelector.StepExecutionRecord("STEP_4", "ERROR")
        );

        List<String> rerunSteps = selector.selectFailedStepsForRerun(records);

        assertEquals(2, rerunSteps.size());
        assertTrue(rerunSteps.contains("STEP_2"));
        assertTrue(rerunSteps.contains("STEP_4"));
        assertFalse(rerunSteps.contains("STEP_1"));
    }

    @Test
    void testAllPassedReturnsEmpty() {
        List<FailedTestRerunSelector.StepExecutionRecord> records = List.of(
                new FailedTestRerunSelector.StepExecutionRecord("STEP_1", "PASSED"),
                new FailedTestRerunSelector.StepExecutionRecord("STEP_2", "PASSED")
        );

        List<String> rerunSteps = selector.selectFailedStepsForRerun(records);
        assertTrue(rerunSteps.isEmpty());
    }
}
