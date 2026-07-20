package com.aiqaos.execution.component;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ArtifactManager
 *
 * Central in-memory registry for execution artifacts.
 * Provides two lookup strategies:
 *  1. By executionId  (UUID) — legacy lookup used by execution pipelines
 *  2. By testCaseId   (string e.g. "TC-AL-003") — used by the dashboard UI
 *
 * Artifacts are also stored with browser and run-number metadata
 * to support the execution history view in the dashboard.
 */
@Component
public class ArtifactManager {

    // Legacy: executionId → list of raw file paths (maintained for backward compatibility)
    private final Map<String, List<String>> artifacts = new ConcurrentHashMap<>();

    // New: testCaseId → ordered list of per-run artifact records (most-recent last)
    private final Map<String, List<TestArtifacts>> testCaseArtifacts = new ConcurrentHashMap<>();

    // ── Legacy API (backward compatible) ──────────────────────────────────────

    public void addArtifact(String executionId, String filePath) {
        artifacts.computeIfAbsent(executionId, k -> new ArrayList<>()).add(filePath);
    }

    public List<String> getArtifacts(String executionId) {
        return artifacts.getOrDefault(executionId, new ArrayList<>());
    }

    // ── Per-test-case artifact registration ───────────────────────────────────

    /**
     * Registers a set of Playwright artifacts for a specific test case run.
     * Each call appends a new TestArtifacts entry (one per execution run),
     * enabling the dashboard to show execution history.
     *
     * @param testCaseId  The test case identifier (e.g. "TC-AL-003")
     * @param artifacts   The artifact paths and metadata for this run
     */
    public void registerTestCaseArtifacts(String testCaseId, TestArtifacts artifacts) {
        testCaseArtifacts.computeIfAbsent(testCaseId, k -> new ArrayList<>()).add(artifacts);
    }

    /**
     * Returns the latest artifact record for a test case (most recent run).
     * Returns null if no artifacts are registered for the given test case.
     */
    public TestArtifacts getLatestArtifacts(String testCaseId) {
        List<TestArtifacts> history = testCaseArtifacts.get(testCaseId);
        if (history == null || history.isEmpty()) return null;
        return history.get(history.size() - 1);
    }

    /**
     * Returns the full execution history for a test case.
     * Each entry represents one execution run (ordered oldest → newest).
     */
    public List<TestArtifacts> getArtifactHistory(String testCaseId) {
        return testCaseArtifacts.getOrDefault(testCaseId, new ArrayList<>());
    }

    /**
     * Checks whether any artifacts have been registered for a test case.
     */
    public boolean hasArtifacts(String testCaseId) {
        List<TestArtifacts> history = testCaseArtifacts.get(testCaseId);
        return history != null && !history.isEmpty();
    }

    // ── Immutable artifact data record ────────────────────────────────────────

    /**
     * Holds the artifact paths for a single Playwright test execution run.
     * Paths are absolute filesystem paths to the generated files.
     */
    public static final class TestArtifacts {

        private final String testCaseId;
        private final String testTitle;
        private final String status;         // "passed" | "failed" | "skipped"
        private final String screenshotPath; // null if test passed (only-on-failure)
        private final String videoPath;      // null if test passed (retain-on-failure)
        private final String tracePath;      // null if test passed (retain-on-failure)
        private final String logPath;        // stderr / stdout log file path
        private final String browser;        // "chromium" | "firefox" | "webkit"
        private final String executionId;    // e.g. "exec-abc12345"
        private final int    runNumber;      // 1-based execution history index

        public TestArtifacts(
            String testCaseId, String testTitle, String status,
            String screenshotPath, String videoPath, String tracePath, String logPath,
            String browser, String executionId, int runNumber
        ) {
            this.testCaseId     = testCaseId;
            this.testTitle      = testTitle;
            this.status         = status;
            this.screenshotPath = screenshotPath;
            this.videoPath      = videoPath;
            this.tracePath      = tracePath;
            this.logPath        = logPath;
            this.browser        = browser;
            this.executionId    = executionId;
            this.runNumber      = runNumber;
        }

        public String getTestCaseId()     { return testCaseId; }
        public String getTestTitle()      { return testTitle; }
        public String getStatus()         { return status; }
        public String getScreenshotPath() { return screenshotPath; }
        public String getVideoPath()      { return videoPath; }
        public String getTracePath()      { return tracePath; }
        public String getLogPath()        { return logPath; }
        public String getBrowser()        { return browser; }
        public String getExecutionId()    { return executionId; }
        public int    getRunNumber()      { return runNumber; }

        public boolean isFailed()         { return "failed".equalsIgnoreCase(status); }
        public boolean hasScreenshot()    { return screenshotPath != null; }
        public boolean hasVideo()         { return videoPath != null; }
        public boolean hasTrace()         { return tracePath != null; }
        public boolean hasLog()           { return logPath != null; }
    }
}