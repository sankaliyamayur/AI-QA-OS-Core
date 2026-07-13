package com.aiqaos.workflow.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "workflows")
public class WorkflowEntity extends BaseEntity {

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