package com.aiqaos.execution.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "execution_artifacts")
public class ExecutionArtifactEntity extends BaseEntity {

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

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

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }
    public String getTracePath() { return tracePath; }
    public void setTracePath(String tracePath) { this.tracePath = tracePath; }
    public String getLogPath() { return logPath; }
    public void setLogPath(String logPath) { this.logPath = logPath; }
    public String getReportPath() { return reportPath; }
    public void setReportPath(String reportPath) { this.reportPath = reportPath; }
}