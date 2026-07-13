package com.aiqaos.intelligence.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "prompt_executions")
public class PromptExecutionEntity extends BaseEntity {

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