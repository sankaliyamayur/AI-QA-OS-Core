package com.aiqaos.execution.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import org.hibernate.annotations.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "execution_steps")
public class ExecutionStepEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-C: tenant discriminator — Hibernate stamps on insert, filters on read (ADR-054).
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "screenshot_url")
    private String screenshotUrl;

    @Column(name = "log_content")
    private String logContent;

    @Column(name = "retry_count")
    private int retryCount;

    @Column(name = "duration_ms")
    private long duration;

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getScreenshotUrl() { return screenshotUrl; }
    public void setScreenshotUrl(String screenshotUrl) { this.screenshotUrl = screenshotUrl; }
    public String getLogContent() { return logContent; }
    public void setLogContent(String logContent) { this.logContent = logContent; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
}