package com.aiqaos.dashboard.service;

import com.aiqaos.dashboard.dto.PromptHistoryEntryDTO;
import com.aiqaos.intelligence.entity.PromptExecutionEntity;
import com.aiqaos.intelligence.repository.PromptExecutionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * FI-PE3-C: PE-3's per-execution prompt history — which prompt template/version actually ran, when,
 * how long it took to render, and which workflow run it belonged to.
 *
 * <p>Reads {@code prompt_executions}, which is filled by {@code PromptExecutionRecorder} on the live
 * render path. Until that producer landed this table had no writer at all, which is why the history
 * half of PE-3 stayed deferred (see ADR-063's rule against producerless read-models).
 *
 * <p>Paging and ordering are pushed to the database: this table gains a row per prompt render, so
 * {@code findAll()}-then-sort would degrade as history accumulates.
 */
@Service
public class PromptHistoryService {

    private final PromptExecutionRepository repository;
    private final int defaultLimit;
    private final int maxLimit;
    private final int previewChars;

    public PromptHistoryService(PromptExecutionRepository repository,
                                @Value("${aiqaos.prompt.history.default-limit:50}") int defaultLimit,
                                @Value("${aiqaos.prompt.history.max-limit:500}") int maxLimit,
                                @Value("${aiqaos.prompt.history.preview-chars:200}") int previewChars) {
        this.repository = repository;
        this.defaultLimit = defaultLimit;
        this.maxLimit = maxLimit;
        this.previewChars = previewChars;
    }

    /** The most recent renders, newest first. {@code limit} is clamped so a caller cannot ask for the world. */
    public List<PromptHistoryEntryDTO> recent(Integer limit) {
        int size = clamp(limit);
        return repository.findAll(PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Every prompt rendered during one workflow run, newest first. A blank correlation returns empty
     * rather than falling back to "everything" — the caller asked about a specific run.
     */
    public List<PromptHistoryEntryDTO> forCorrelation(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            return List.of();
        }
        return repository.findByCorrelationIdOrderByCreatedAtDesc(correlationId).stream()
                .map(this::toDto)
                .toList();
    }

    private int clamp(Integer limit) {
        int requested = (limit == null || limit <= 0) ? defaultLimit : limit;
        return Math.min(requested, Math.max(1, maxLimit));
    }

    private PromptHistoryEntryDTO toDto(PromptExecutionEntity e) {
        String prompt = e.getFinalCompiledPrompt() == null ? "" : e.getFinalCompiledPrompt();
        return new PromptHistoryEntryDTO(
                e.getId(),
                e.getTemplateName(),
                e.getVersionLabel(),
                e.getCorrelationId(),
                e.getTraceId(),
                e.getResponseTimeMs(),
                prompt.length(),
                preview(prompt),
                e.getCreatedAt());
    }

    /** See {@link PromptHistoryEntryDTO} — this surface is unauthenticated, so never emit the full prompt. */
    private String preview(String prompt) {
        if (previewChars <= 0 || prompt.length() <= previewChars) {
            return prompt;
        }
        return prompt.substring(0, previewChars) + "…";
    }
}
