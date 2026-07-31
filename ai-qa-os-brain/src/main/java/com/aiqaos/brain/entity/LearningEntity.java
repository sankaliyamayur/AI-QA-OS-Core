package com.aiqaos.brain.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import org.hibernate.annotations.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "brain_learning")
public class LearningEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-C ext: tenant discriminator (ADR-054/057) — Hibernate stamps on insert, filters on read.
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    @Column(name = "pattern_name", nullable = false)
    private String pattern;

    @Column(name = "previous_decision")
    private String previousDecision;

    @Column(name = "result")
    private String result;

    @Column(name = "improvement")
    private String improvement;

    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }
    public String getPreviousDecision() { return previousDecision; }
    public void setPreviousDecision(String previousDecision) { this.previousDecision = previousDecision; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getImprovement() { return improvement; }
    public void setImprovement(String improvement) { this.improvement = improvement; }
}