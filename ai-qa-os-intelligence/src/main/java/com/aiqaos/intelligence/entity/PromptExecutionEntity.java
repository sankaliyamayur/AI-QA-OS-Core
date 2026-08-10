package com.aiqaos.intelligence.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import org.hibernate.annotations.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    // FI-PE3-C (V23): TEXT, not @Lob. Hibernate maps @Lob String to a Postgres large object (oid), which
    // is not deleted with its row — a per-render producer would leak one large object per prompt.
    @Column(name = "final_compiled_prompt", columnDefinition = "TEXT", nullable = false)
    private String finalCompiledPrompt;

    // FI-PE3-C (V23): the resolved prompt identity. template_id/version_id are populated only when the
    // template is registered in prompt_templates; prompts load from the classpath, so without these the
    // history would be anonymous.
    @Column(name = "template_name", length = 200)
    private String templateName;

    @Column(name = "version_label", length = 100)
    private String versionLabel;

    // FI-PE3-C (V23): the run key — MNT-6's pipeline correlationId, tying a prompt to its workflow run.
    @Column(name = "correlation_id", length = 100)
    private String correlationId;

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

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getVersionLabel() { return versionLabel; }
    public void setVersionLabel(String versionLabel) { this.versionLabel = versionLabel; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
}