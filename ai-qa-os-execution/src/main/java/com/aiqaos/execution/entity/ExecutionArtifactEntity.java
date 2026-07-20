package com.aiqaos.execution.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * ExecutionArtifactEntity
 *
 * Persists the artifact paths produced by a single Playwright test execution run.
 * Each row maps to one test case + one execution run + one browser.
 *
 * Columns added beyond the original schema:
 *  - test_case_id : enables the dashboard UI to query artifacts directly by test case ID
 *                   (e.g. "TC-AL-003") without needing the executionId UUID
 *  - browser      : supports multi-browser artifact organization (chromium/firefox/webkit)
 *  - run_number   : 1-based counter supporting execution history (Run #1, Run #2…)
 */
@Entity
@Table(name = "execution_artifacts")
public class ExecutionArtifactEntity extends BaseEntity {

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    /** Human-readable test case identifier, e.g. "TC-AL-003". Used by dashboard UI. */
    @Column(name = "test_case_id", length = 64)
    private String testCaseId;

    /** Browser project this artifact belongs to: chromium | firefox | webkit */
    @Column(name = "browser", length = 32)
    private String browser;

    /** 1-based execution history index. Incremented each time the test case is re-run. */
    @Column(name = "run_number")
    private int runNumber = 1;

    @Column(name = "screenshot_path")
    private String screenshotPath;

    @Column(name = "video_path")
    private String videoPath;

    @Column(name = "trace_path")
    private String tracePath;

    @Column(name = "log_path")
    private String logPath;

    @Column(name = "report_path")
    private String reportPath;

    // ── Getters and setters ────────────────────────────────────────────────────

    public UUID getExecutionId()           { return executionId; }
    public void setExecutionId(UUID v)     { this.executionId = v; }

    public String getTestCaseId()          { return testCaseId; }
    public void setTestCaseId(String v)    { this.testCaseId = v; }

    public String getBrowser()             { return browser; }
    public void setBrowser(String v)       { this.browser = v; }

    public int getRunNumber()              { return runNumber; }
    public void setRunNumber(int v)        { this.runNumber = v; }

    public String getScreenshotPath()      { return screenshotPath; }
    public void setScreenshotPath(String v){ this.screenshotPath = v; }

    public String getVideoPath()           { return videoPath; }
    public void setVideoPath(String v)     { this.videoPath = v; }

    public String getTracePath()           { return tracePath; }
    public void setTracePath(String v)     { this.tracePath = v; }

    public String getLogPath()             { return logPath; }
    public void setLogPath(String v)       { this.logPath = v; }

    public String getReportPath()          { return reportPath; }
    public void setReportPath(String v)    { this.reportPath = v; }
}