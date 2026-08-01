package com.aiqaos.dashboard.controller;

import com.aiqaos.dashboard.service.PromptQualityService;
import com.aiqaos.eval.benchmark.PromptQualitySummary;
import com.aiqaos.eval.benchmark.PromptRegressionReport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    public PromptQualityController(PromptQualityService promptQualityService) {
        this.promptQualityService = promptQualityService;
    }

    @GetMapping
    public PromptQualitySummary getSummary() {
        return promptQualityService.getSummary();
    }

    @GetMapping("/regressions")
    public PromptRegressionReport getRegressions() {
        return promptQualityService.getRegressions();
    }
}
