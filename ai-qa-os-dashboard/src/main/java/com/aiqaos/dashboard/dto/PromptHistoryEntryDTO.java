package com.aiqaos.dashboard.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FI-PE3-C: one recorded prompt render in PE-3's per-execution history.
 *
 * <p><b>Carries a preview, not the prompt.</b> The dashboard's own filter chain {@code permitAll}s
 * {@code /api/dashboard/**} with no JWT filter (see ADR-067, which is why admin writes were hosted
 * outside it), so this surface is unauthenticated. Compiled prompts embed injected context — user
 * stories, failure output, and whatever else the pipeline fed the model — so the full text is
 * deliberately not exposed here; the preview plus {@code promptLength} is enough to identify and
 * compare renders. {@code correlationId} ties the render back to its workflow run.
 */
public record PromptHistoryEntryDTO(
        UUID id,
        String templateName,
        String versionLabel,
        String correlationId,
        String traceId,
        long responseTimeMs,
        int promptLength,
        String promptPreview,
        LocalDateTime executedAt) {
}
