package com.aiqaos.dashboard.controller;

import com.aiqaos.dashboard.dto.HealingAnalyticsSummary;
import com.aiqaos.dashboard.dto.HealingMetricDTO;
import com.aiqaos.dashboard.dto.LocatorDriftEntry;
import com.aiqaos.dashboard.service.HealingAnalyticsService;
import com.aiqaos.dashboard.service.LocatorDriftService;
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
    private final LocatorDriftService locatorDriftService;

    public HealingAnalyticsController(HealingAnalyticsService healingAnalyticsService,
                                      LocatorDriftService locatorDriftService) {
        this.healingAnalyticsService = healingAnalyticsService;
        this.locatorDriftService = locatorDriftService;
    }

    @GetMapping
    public List<HealingMetricDTO> getByExecutionId(@RequestParam("executionId") UUID executionId) {
        return healingAnalyticsService.getByExecutionId(executionId);
    }

    @GetMapping("/summary")
    public Map<String, Long> getActionTypeBreakdown() {
        return healingAnalyticsService.getActionTypeBreakdown();
    }

    /** HEAL-3: the full healing-analytics read-model (counts, success rate, avg improvement, breakdowns). */
    @GetMapping("/analytics")
    public HealingAnalyticsSummary getAnalyticsSummary() {
        return healingAnalyticsService.getSummary();
    }

    /**
     * HEAL-3 (FI-HEAL3-B): the locators that break most often, worst first, with how often a
     * replacement could be proposed for each. Empty until locator drift has actually been observed —
     * the producer is opt-in ({@code aiqaos.healing.locator-drift.enabled}).
     */
    @GetMapping("/locator-drift")
    public List<LocatorDriftEntry> getLocatorDrift(@RequestParam(required = false) Integer limit) {
        return locatorDriftService.topDrifting(limit);
    }
}
