package com.aiqaos.intelligence.manager;

import com.aiqaos.intelligence.entity.PromptExecutionEntity;
import com.aiqaos.intelligence.repository.PromptExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * FI-PE3-C (PE-3): records one {@link PromptExecutionEntity} per prompt render, giving PE-3 a real
 * per-execution prompt history.
 *
 * <p><b>Why this is a genuine producer.</b> {@code prompt_executions} existed since V5 but
 * {@code PromptExecutionRepository} had <b>zero callers</b> — the table could never fill. Unlike the
 * LRN-3 / HEAL-3 read-models (deferred as producerless in ADR-063 / ADR-070 / ADR-072), the write
 * point here is on a genuinely live path: {@code PromptManagerImpl.renderPrompt} is called by all
 * eight production agents on every pipeline run.
 *
 * <p><b>Every field is observed, never derived.</b> Template name and version label come from the
 * request and {@link PromptVersionManager}; the compiled text is the text actually sent; the elapsed
 * time is measured. The run key is MNT-6's pipeline {@code correlationId} read from the MDC — the same
 * value every log line for that run carries — because agents build their {@code PromptRequest} without
 * populating its metadata, so the request itself carries no correlation. When a render happens outside
 * a pipeline run the MDC is empty and the column is simply null: absent, not invented (ADR-063).
 *
 * <p><b>Opt-in and best-effort.</b> Registered only when {@code aiqaos.prompt.history.enabled=true},
 * so existing deployments gain no writes by default. Recording never throws: history is diagnostics,
 * and losing a history row must never fail the render that produced it.
 */
@Component
@ConditionalOnProperty(name = "aiqaos.prompt.history.enabled", havingValue = "true")
public class PromptExecutionRecorder {

    /** MNT-6 / CorrelationIdFilter MDC key — the workflow run's correlation id. */
    static final String MDC_CORRELATION_ID = "correlationId";
    static final String MDC_TRACE_ID = "traceId";

    private static final Logger log = LoggerFactory.getLogger(PromptExecutionRecorder.class);

    private final PromptExecutionRepository repository;
    private final int maxPromptChars;

    public PromptExecutionRecorder(PromptExecutionRepository repository,
                                   @Value("${aiqaos.prompt.history.max-prompt-chars:20000}") int maxPromptChars) {
        this.repository = repository;
        this.maxPromptChars = maxPromptChars;
    }

    /**
     * Persist one render. Never throws — a failure here is logged and swallowed so prompt rendering,
     * and therefore the whole pipeline, is unaffected by history being unavailable.
     */
    public void record(String templateName, String versionLabel, String compiledPrompt, long elapsedMs) {
        try {
            PromptExecutionEntity entity = new PromptExecutionEntity();
            entity.setTemplateName(templateName);
            entity.setVersionLabel(versionLabel);
            entity.setFinalCompiledPrompt(truncate(compiledPrompt));
            entity.setResponseTimeMs(elapsedMs);
            entity.setCorrelationId(MDC.get(MDC_CORRELATION_ID));
            entity.setTraceId(MDC.get(MDC_TRACE_ID));
            // tenant_id is stamped by Hibernate's @TenantId discriminator (ADR-054/057).
            repository.save(entity);
        } catch (Exception e) {
            log.warn("[prompt-history] failed to record render of {} ({}): {}",
                    templateName, versionLabel, e.getMessage());
        }
    }

    /**
     * Cap the stored prompt so one pathological render cannot write an unbounded row. The column is
     * TEXT, so this is a sanity bound rather than a storage limit; truncation is marked so a reader
     * never mistakes a clipped prompt for the whole one.
     */
    private String truncate(String prompt) {
        if (prompt == null) {
            return ""; // the column is NOT NULL; an empty render is recorded as empty, not skipped
        }
        if (maxPromptChars <= 0 || prompt.length() <= maxPromptChars) {
            return prompt;
        }
        return prompt.substring(0, maxPromptChars) + "\n…[truncated " + (prompt.length() - maxPromptChars) + " chars]";
    }
}
