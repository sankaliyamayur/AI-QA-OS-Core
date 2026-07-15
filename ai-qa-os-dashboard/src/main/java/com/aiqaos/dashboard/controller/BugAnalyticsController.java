package com.aiqaos.dashboard.controller;

import com.aiqaos.workflow.model.BugAnalysisDTO;
import com.aiqaos.workflow.service.BugAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard/bugs")
public class BugAnalyticsController {

    private final BugAnalyticsService bugAnalyticsService;

    public BugAnalyticsController(BugAnalyticsService bugAnalyticsService) {
        this.bugAnalyticsService = bugAnalyticsService;
    }

    @GetMapping
    public List<BugAnalysisDTO> getByExecutionId(@RequestParam("executionId") UUID executionId) {
        return bugAnalyticsService.getByExecutionId(executionId);
    }

    @GetMapping("/breakdown")
    public Map<String, Long> getFailureCategoryBreakdown() {
        return bugAnalyticsService.getFailureCategoryBreakdown();
    }
}
