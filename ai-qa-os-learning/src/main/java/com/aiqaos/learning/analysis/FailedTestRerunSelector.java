package com.aiqaos.learning.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class FailedTestRerunSelector {

    private static final Logger log = LoggerFactory.getLogger(FailedTestRerunSelector.class);

    public List<String> selectFailedStepsForRerun(List<StepExecutionRecord> previousExecutionSteps) {
        if (previousExecutionSteps == null || previousExecutionSteps.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> failedStepNames = previousExecutionSteps.stream()
                .filter(step -> step != null && ("FAILED".equalsIgnoreCase(step.getStatus()) || "ERROR".equalsIgnoreCase(step.getStatus())))
                .map(StepExecutionRecord::getStepName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("WF-3: Selected {} failed steps out of {} total steps for re-run: {}",
                failedStepNames.size(), previousExecutionSteps.size(), failedStepNames);

        return failedStepNames;
    }

    public static class StepExecutionRecord {
        private String stepName;
        private String status;

        public StepExecutionRecord() {
        }

        public StepExecutionRecord(String stepName, String status) {
            this.stepName = stepName;
            this.status = status;
        }

        public String getStepName() {
            return stepName;
        }

        public void setStepName(String stepName) {
            this.stepName = stepName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
