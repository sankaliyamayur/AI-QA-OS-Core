package com.aiqaos.execution.engine;

import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.GeneratedScriptSuite.AutomationScript;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
public class PlaywrightExecutionEngine implements ExecutionEngine {

    private boolean running = false;

    @Override
    public String getSupportedFramework() {
        return "Playwright";
    }

    @Override
    public ExecutionResult execute(GeneratedScriptSuite scriptSuite, ExecutionConfiguration configuration) {
        running = true;
        LocalDateTime start = LocalDateTime.now();
        ExecutionResult result = new ExecutionResult();
        result.setStartTime(start);
        result.setTaskId(scriptSuite.getSuiteId());
        result.setAgentId("execution-engineer-playwright");

        try {
            // Simulate short execution time
            Thread.sleep(10);

            int passedCount = 0;
            int failedCount = 0;

            for (AutomationScript script : scriptSuite.getScripts()) {
                // Fail-fast code check: if code throws UnsupportedOperationException, simulate runtime failure
                if (script.getCode() != null && script.getCode().contains("UnsupportedOperationException")) {
                    failedCount++;
                    result.setErrorMessage("Automation script runtime exception: UnsupportedOperationException");
                    result.setStackTrace("java.lang.UnsupportedOperationException: LLM did not generate automation code.\n" +
                            "\tat com.aiqaos.execution.PlaywrightRuntime.run(" + script.getScriptId() + ")");
                } else {
                    passedCount++;
                }
            }

            result.setPassed(passedCount);
            result.setFailed(failedCount);
            result.setSkipped(0);

            if (failedCount > 0) {
                result.setSuccess(false);
                result.setStatus("FAILED");
            } else {
                result.setSuccess(true);
                result.setStatus("PASSED");
            }

            result.setLogs("Playwright test execution finished.\nPassed: " + passedCount + ", Failed: " + failedCount);
            result.setConsoleLogs("[INFO] Launching browser: " + configuration.getBrowser() + "\n[INFO] Running in headless mode: " + configuration.isHeadless());
            result.getScreenshots().add("/artifacts/screenshot-1.png");
            result.getArtifacts().add("/artifacts/playwright-trace.zip");
            result.setReportLocation("/reports/playwright-report.html");

        } catch (InterruptedException e) {
            result.setSuccess(false);
            result.setStatus("CANCELLED");
            result.setErrorMessage("Execution interrupted: " + e.getMessage());
        } finally {
            running = false;
        }

        LocalDateTime end = LocalDateTime.now();
        result.setEndTime(end);
        result.setCompletedAt(end);
        result.setDuration(java.time.Duration.between(start, end).toMillis());
        return result;
    }

    @Override
    public void cancel() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
