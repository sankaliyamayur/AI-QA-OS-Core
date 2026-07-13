package com.aiqaos.execution.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "execution_audits")
public class ExecutionAuditEntity extends BaseEntity {

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "action_name", nullable = false)
    private String action;

    @Column(name = "locator")
    private String locator;

    @Column(name = "command_details")
    private String command;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "duration_ms")
    private long duration;

    @Column(name = "exception_message")
    private String exceptionMessage;

    @Column(name = "screenshot_ref")
    private String screenshotReference;

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getLocator() { return locator; }
    public void setLocator(String locator) { this.locator = locator; }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    public String getExceptionMessage() { return exceptionMessage; }
    public void setExceptionMessage(String exceptionMessage) { this.exceptionMessage = exceptionMessage; }
    public String getScreenshotReference() { return screenshotReference; }
    public void setScreenshotReference(String screenshotReference) { this.screenshotReference = screenshotReference; }
}