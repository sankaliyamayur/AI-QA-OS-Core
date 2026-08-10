package com.aiqaos.dashboard.controller;

import com.aiqaos.dashboard.dto.PromptHistoryEntryDTO;
import com.aiqaos.dashboard.service.PromptHistoryService;
import com.aiqaos.dashboard.service.PromptQualityService;
import com.aiqaos.eval.benchmark.PromptQualitySummary;
import com.aiqaos.eval.benchmark.PromptRegressionReport;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PE-3 (ADR-062): read-only prompt-quality dashboard. Exposes the {@link PromptQualitySummary}
 * aggregated from persisted evaluation results (no benchmark re-run). FI-PE3-B (ADR-069) adds the
 * {@link PromptRegressionReport} — versions that declined over time — from the same data.
 */
@RestController
@RequestMapping("/api/dashboard/prompt-quality")
public class PromptQualityController {

    private final PromptQualityService promptQualityService;
    private final PromptHistoryService promptHistoryService;

    public PromptQualityController(PromptQualityService promptQualityService,
                                   PromptHistoryService promptHistoryService) {
        this.promptQualityService = promptQualityService;
        this.promptHistoryService = promptHistoryService;
    }

    @GetMapping
    public PromptQualitySummary getSummary() {
        return promptQualityService.getSummary();
    }

    @GetMapping("/regressions")
    public PromptRegressionReport getRegressions() {
        return promptQualityService.getRegressions();
    }

    /**
     * FI-PE3-C: per-execution prompt history. With {@code correlationId} it returns every prompt
     * rendered during that workflow run; without it, the most recent renders across all runs.
     * Entries carry a prompt <i>preview</i> only — see {@link PromptHistoryEntryDTO}.
     */
    @GetMapping("/history")
    public List<PromptHistoryEntryDTO> getHistory(
            @RequestParam(required = false) String correlationId,
            @RequestParam(required = false) Integer limit) {
        return (correlationId == null || correlationId.isBlank())
                ? promptHistoryService.recent(limit)
                : promptHistoryService.forCorrelation(correlationId);
    }
}
