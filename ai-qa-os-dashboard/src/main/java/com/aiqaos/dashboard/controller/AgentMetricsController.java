package com.aiqaos.dashboard.controller;

import com.aiqaos.dashboard.dto.AgentMetricDTO;
import com.aiqaos.workflow.service.AgentMetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard/agents/metrics")
public class AgentMetricsController {

    private final AgentMetricsService agentMetricsService;

    public AgentMetricsController(AgentMetricsService agentMetricsService) {
        this.agentMetricsService = agentMetricsService;
    }

    @GetMapping
    public List<AgentMetricDTO> getByExecutionId(@RequestParam("executionId") UUID executionId) {
        return agentMetricsService.getByExecutionId(executionId).stream()
                .map(AgentMetricDTO::from)
                .toList();
    }
}
