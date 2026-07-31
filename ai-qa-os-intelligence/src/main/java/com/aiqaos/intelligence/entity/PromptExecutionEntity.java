package com.aiqaos.intelligence.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import org.hibernate.annotations.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "prompt_executions")
public class PromptExecutionEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-C ext: tenant discriminator (ADR-054/057) — Hibernate stamps on insert, filters on read.
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "version_id")
    private UUID versionId;

    @Lob
    @Column(name = "final_compiled_prompt", nullable = false)
    private String finalCompiledPrompt;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "response_time_ms")
    private long responseTimeMs;

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }

    public UUID getVersionId() { return versionId; }
    public void setVersionId(UUID versionId) { this.versionId = versionId; }

    public String getFinalCompiledPrompt() { return finalCompiledPrompt; }
    public void setFinalCompiledPrompt(String finalCompiledPrompt) { this.finalCompiledPrompt = finalCompiledPrompt; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
}