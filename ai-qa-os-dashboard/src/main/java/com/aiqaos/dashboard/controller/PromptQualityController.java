package com.aiqaos.dashboard.controller;

import com.aiqaos.dashboard.service.PromptQualityService;
import com.aiqaos.eval.benchmark.PromptQualitySummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PE-3 (ADR-062): read-only prompt-quality dashboard. Exposes the {@link PromptQualitySummary}
 * aggregated from persisted evaluation results (no benchmark re-run).
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
}
