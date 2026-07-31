package com.aiqaos.orchestration.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import org.hibernate.annotations.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "workflow_steps")
public class WorkflowStepEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-C ext: tenant discriminator (ADR-054/057) — Hibernate stamps on insert, filters on read.
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "agent_type", nullable = false)
    private String agentType;

    @Column(name = "execution_order", nullable = false)
    private int executionOrder;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "timeout_seconds")
    private long timeout;

    @Column(name = "condition_rule")
    private String condition;

    @Column(name = "is_parallel")
    private boolean parallel;

    public UUID getWorkflowId() { return workflowId; }
    public void setWorkflowId(UUID workflowId) { this.workflowId = workflowId; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getAgentType() { return agentType; }
    public void setAgentType(String agentType) { this.agentType = agentType; }
    public int getExecutionOrder() { return executionOrder; }
    public void setExecutionOrder(int executionOrder) { this.executionOrder = executionOrder; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public long getTimeout() { return timeout; }
    public void setTimeout(long timeout) { this.timeout = timeout; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public boolean isParallel() { return parallel; }
    public void setParallel(boolean parallel) { this.parallel = parallel; }
}