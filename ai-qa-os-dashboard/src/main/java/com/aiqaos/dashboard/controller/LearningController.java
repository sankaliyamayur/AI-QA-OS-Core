package com.aiqaos.dashboard.controller;

import com.aiqaos.dashboard.service.LearningDashboardService;
import com.aiqaos.learning.dashboard.LearningDashboardView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * LRN-3: read-only learning-loop dashboard. Serves the {@link LearningDashboardView} — learning
 * score, success rate, average confidence, the confidence-history series, the trend, and the
 * HEALTHY/AT_RISK signal — computed from the observations the run pipeline records.
 */
@RestController
@RequestMapping("/api/dashboard/learning")
public class LearningController {

    private final LearningDashboardService learningDashboardService;

    public LearningController(LearningDashboardService learningDashboardService) {
        this.learningDashboardService = learningDashboardService;
    }

    /**
     * @param limit how many of the most recent observed runs to measure over; omitted uses the
     *              configured default and any value is clamped to the configured maximum
     */
    @GetMapping
    public LearningDashboardView getLearningDashboard(@RequestParam(required = false) Integer limit) {
        return learningDashboardService.getView(limit);
    }
}
