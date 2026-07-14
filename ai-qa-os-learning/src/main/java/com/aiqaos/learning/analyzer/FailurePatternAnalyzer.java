package com.aiqaos.learning.analyzer;

import com.aiqaos.core.model.FailurePattern;
import com.aiqaos.core.model.QAExecutionReport;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FailurePatternAnalyzer {

    public List<FailurePattern> analyzeFailures(QAExecutionReport report, List<FailurePattern> history) {
        List<FailurePattern> detected = new ArrayList<>();

        if (report.getBugAnalysisReport() != null && report.getBugAnalysisReport().getRootCause() != null) {
            String rootCause = report.getBugAnalysisReport().getRootCause();
            String errorType = report.getBugAnalysisReport().getFailureCategory() != null 
                    ? report.getBugAnalysisReport().getFailureCategory() 
                    : "GENERIC_ERROR";

            // See if this pattern already exists in history
            int initialCount = 1;
            for (FailurePattern historical : history) {
                if (historical.getErrorType().equals(errorType) && historical.getRootCause().equals(rootCause)) {
                    initialCount = historical.getOccurrenceCount() + 1;
                    break;
                }
            }

            FailurePattern pattern = new FailurePattern();
            pattern.setPatternId("PAT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            pattern.setErrorType(errorType);
            pattern.setRootCause(rootCause);
            pattern.setImpactedComponent(report.getBugAnalysisReport().getImpactedComponent() != null 
                    ? report.getBugAnalysisReport().getImpactedComponent() 
                    : "UNKNOWN");
            pattern.setOccurrenceCount(initialCount);
            pattern.setConfidence(report.getBugAnalysisReport().getConfidence() > 0 
                    ? report.getBugAnalysisReport().getConfidence() 
                    : 0.85);
            pattern.setLastDetected(LocalDateTime.now());

            detected.add(pattern);
        }

        return detected;
    }
}
