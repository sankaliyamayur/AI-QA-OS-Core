package com.aiqaos.orchestration.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import org.hibernate.annotations.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "workflows")
public class WorkflowEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-C ext: tenant discriminator (ADR-054/057) — Hibernate stamps on insert, filters on read.
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    @Column(name = "workflow_name", nullable = false)
    private String workflowName;

    @Column(name = "description")
    private String description;

    @Column(name = "workflow_version", nullable = false)
    private String workflowVersion;

    @Column(name = "status", nullable = false)
    private String status;

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWorkflowVersion() { return workflowVersion; }
    public void setWorkflowVersion(String workflowVersion) { this.workflowVersion = workflowVersion; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}