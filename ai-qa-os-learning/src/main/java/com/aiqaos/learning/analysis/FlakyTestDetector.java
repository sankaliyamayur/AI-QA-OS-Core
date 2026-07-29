package com.aiqaos.learning.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlakyTestDetector {

    private static final Logger log = LoggerFactory.getLogger(FlakyTestDetector.class);

    private final double flakinessThreshold;

    public FlakyTestDetector() {
        this(0.30); // Default 30% flakiness threshold
    }

    public FlakyTestDetector(double flakinessThreshold) {
        this.flakinessThreshold = flakinessThreshold;
    }

    /**
     * Analyzes execution outcomes for a named test step.
     * Outcomes are chronological status strings e.g. ["PASSED", "FAILED", "PASSED"]
     */
    public FlakyTestReport analyzeStepHistory(String stepName, List<String> executionOutcomes) {
        if (executionOutcomes == null || executionOutcomes.isEmpty()) {
            return new FlakyTestReport(stepName, 0, 0, 0, 0.0, FlakyTestReport.Recommendation.STABLE);
        }

        int total = executionOutcomes.size();
        int failures = 0;
        int flips = 0;
        String prevStatus = null;

        for (String status : executionOutcomes) {
            String norm = status != null ? status.toUpperCase() : "UNKNOWN";
            if ("FAILED".equals(norm) || "FAILURE".equals(norm) || "ERROR".equals(norm)) {
                failures++;
            }

            if (prevStatus != null && !prevStatus.equalsIgnoreCase(norm)) {
                flips++;
            }
            prevStatus = norm;
        }

        // Flakiness score combines failure rate and pass/fail flip oscillation rate
        double failureRate = (double) failures / total;
        double flipRate = total > 1 ? (double) flips / (total - 1) : 0.0;

        // High flip rate + mixed pass/fail = high flakiness
        double flakinessScore = (failureRate * 0.4) + (flipRate * 0.6);

        FlakyTestReport.Recommendation rec = FlakyTestReport.Recommendation.STABLE;
        if (flakinessScore >= 0.50 || (flipRate > 0.4 && failureRate > 0.2)) {
            rec = FlakyTestReport.Recommendation.QUARANTINE;
        } else if (flakinessScore >= flakinessThreshold || flipRate > 0.2) {
            rec = FlakyTestReport.Recommendation.FLAKY_RETRY;
        }

        log.debug("WF-3: Analyzed step '{}': total={}, failures={}, flips={}, score={}, rec={}",
                stepName, total, failures, flips, flakinessScore, rec);

        return new FlakyTestReport(stepName, total, failures, flips, flakinessScore, rec);
    }
}
