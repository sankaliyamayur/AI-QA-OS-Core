package com.aiqaos.execution.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import org.hibernate.annotations.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "executions")
public class ExecutionEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-C: tenant discriminator — Hibernate stamps on insert, filters on read (ADR-054).
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "environment")
    private String environment;

    @Column(name = "browser")
    private String browser;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private long duration;

    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
}