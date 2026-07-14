package com.aiqaos.learning.analyzer;

import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.FailurePattern;
import com.aiqaos.core.model.QAExecutionReport;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FailurePatternAnalyzerTest {

    @Test
    void testAnalyzeFailuresCalculatesOccurrenceCount() {
        FailurePatternAnalyzer analyzer = new FailurePatternAnalyzer();

        QAExecutionReport report = new QAExecutionReport();
        report.setOverallResult("FAIL");
        BugAnalysisReport bug = new BugAnalysisReport();
        bug.setFailureCategory("TIMEOUT_EXCEPTION");
        bug.setRootCause("Server took too long to respond");
        bug.setConfidence(0.95);
        report.setBugAnalysisReport(bug);

        List<FailurePattern> history = new ArrayList<>();
        FailurePattern existing = new FailurePattern();
        existing.setErrorType("TIMEOUT_EXCEPTION");
        existing.setRootCause("Server took too long to respond");
        existing.setOccurrenceCount(5);
        history.add(existing);

        List<FailurePattern> result = analyzer.analyzeFailures(report, history);

        assertEquals(1, result.size());
        assertEquals("TIMEOUT_EXCEPTION", result.get(0).getErrorType());
        assertEquals(6, result.get(0).getOccurrenceCount());
        assertEquals(0.95, result.get(0).getConfidence());
    }
}
