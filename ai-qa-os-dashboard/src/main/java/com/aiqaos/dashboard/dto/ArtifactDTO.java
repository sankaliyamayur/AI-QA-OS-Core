package com.aiqaos.dashboard.dto;

import java.util.List;

/**
 * ArtifactDTO
 *
 * Represents all Playwright-generated artifacts associated with a test case.
 * Returned by GET /api/dashboard/artifacts/{testCaseId}.
 *
 * The dashboard UI receives this DTO and renders:
 *  - screenshotUrl  → ScreenshotViewer
 *  - videoUrl       → VideoPlayer
 *  - traceUrl       → AttachmentViewer (download link)
 *  - htmlReportUrl  → AttachmentViewer (download link)
 *  - consoleLog     → ExecutionLogs (stdout/stderr panel)
 *  - stackTrace     → ExecutionLogs (stack trace panel)
 *  - networkLog     → AttachmentViewer (embedded panel)
 *  - history        → ExecutionHistoryPanel (per-run artifacts)
 */
public class ArtifactDTO {

    /** Test case identifier, e.g. "TC-AL-003" */
    private String testCaseId;

    /** Browser used for this run: chromium | firefox | webkit */
    private String browser;

    /** Execution status: passed | failed | skipped */
    private String status;

    /** HTTP URL to access the failure screenshot (null if test passed) */
    private String screenshotUrl;

    /** HTTP URL to stream/download the execution video (null if test passed) */
    private String videoUrl;

    /** HTTP URL to download the Playwright trace zip */
    private String traceUrl;

    /** HTTP URL to open the Playwright HTML report */
    private String htmlReportUrl;

    /** Playwright stdout / console log text captured during execution */
    private String consoleLog;

    /** Exception stack trace if the test failed */
    private String stackTrace;

    /** Network request/response log captured during execution */
    private String networkLog;

    /**
     * Execution history for this test case.
     * Each entry represents one run — ordered oldest → newest.
     * Enables the "Run #1 / Run #2 / Run #3" history view in the dashboard.
     */
    private List<RunEntry> history;

    // ── Getters and setters ────────────────────────────────────────────────────

    public String getTestCaseId()             { return testCaseId; }
    public void setTestCaseId(String v)       { this.testCaseId = v; }

    public String getBrowser()                { return browser; }
    public void setBrowser(String v)          { this.browser = v; }

    public String getStatus()                 { return status; }
    public void setStatus(String v)           { this.status = v; }

    public String getScreenshotUrl()          { return screenshotUrl; }
    public void setScreenshotUrl(String v)    { this.screenshotUrl = v; }

    public String getVideoUrl()               { return videoUrl; }
    public void setVideoUrl(String v)         { this.videoUrl = v; }

    public String getTraceUrl()               { return traceUrl; }
    public void setTraceUrl(String v)         { this.traceUrl = v; }

    public String getHtmlReportUrl()          { return htmlReportUrl; }
    public void setHtmlReportUrl(String v)    { this.htmlReportUrl = v; }

    public String getConsoleLog()             { return consoleLog; }
    public void setConsoleLog(String v)       { this.consoleLog = v; }

    public String getStackTrace()             { return stackTrace; }
    public void setStackTrace(String v)       { this.stackTrace = v; }

    public String getNetworkLog()             { return networkLog; }
    public void setNetworkLog(String v)       { this.networkLog = v; }

    public List<RunEntry> getHistory()        { return history; }
    public void setHistory(List<RunEntry> v)  { this.history = v; }

    // ── Nested run-history entry ───────────────────────────────────────────────

    /**
     * Represents a single historical execution run for a test case.
     * Used to populate the execution history timeline in the dashboard.
     */
    public static class RunEntry {
        private int    runNumber;      // 1-based
        private String executionId;
        private String browser;
        private String status;         // passed | failed | skipped
        private String screenshotUrl;
        private String videoUrl;
        private String traceUrl;

        public int    getRunNumber()             { return runNumber; }
        public void   setRunNumber(int v)        { this.runNumber = v; }
        public String getExecutionId()           { return executionId; }
        public void   setExecutionId(String v)   { this.executionId = v; }
        public String getBrowser()               { return browser; }
        public void   setBrowser(String v)       { this.browser = v; }
        public String getStatus()                { return status; }
        public void   setStatus(String v)        { this.status = v; }
        public String getScreenshotUrl()         { return screenshotUrl; }
        public void   setScreenshotUrl(String v) { this.screenshotUrl = v; }
        public String getVideoUrl()              { return videoUrl; }
        public void   setVideoUrl(String v)      { this.videoUrl = v; }
        public String getTraceUrl()              { return traceUrl; }
        public void   setTraceUrl(String v)      { this.traceUrl = v; }
    }
}
