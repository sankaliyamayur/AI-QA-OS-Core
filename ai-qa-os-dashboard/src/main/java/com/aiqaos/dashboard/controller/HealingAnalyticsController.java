package com.aiqaos.dashboard.controller;

import com.aiqaos.dashboard.dto.HealingMetricDTO;
import com.aiqaos.dashboard.service.HealingAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard/healing")
public class HealingAnalyticsController {

    private final HealingAnalyticsService healingAnalyticsService;

    public HealingAnalyticsController(HealingAnalyticsService healingAnalyticsService) {
        this.healingAnalyticsService = healingAnalyticsService;
    }

    @GetMapping
    public List<HealingMetricDTO> getByExecutionId(@RequestParam("executionId") UUID executionId) {
        return healingAnalyticsService.getByExecutionId(executionId);
    }

    @GetMapping("/summary")
    public Map<String, Long> getActionTypeBreakdown() {
        return healingAnalyticsService.getActionTypeBreakdown();
    }
}
