package com.aiqaos.dashboard.service;

import com.aiqaos.dashboard.dto.HealingAnalyticsSummary;
import com.aiqaos.dashboard.dto.HealingMetricDTO;
import com.aiqaos.observability.entity.HealingMetricEntity;
import com.aiqaos.observability.repository.HealingMetricRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class HealingAnalyticsService {

    private final HealingMetricRepository healingMetricRepository;
    private final HealingAnalyticsAssembler assembler;

    public HealingAnalyticsService(HealingMetricRepository healingMetricRepository,
                                   HealingAnalyticsAssembler assembler) {
        this.healingMetricRepository = healingMetricRepository;
        this.assembler = assembler;
    }

    /** HEAL-3: the healing analytics read-model — success rate + breakdowns over all heals. */
    public HealingAnalyticsSummary getSummary() {
        return assembler.summarize(healingMetricRepository.findAll());
    }

    public List<HealingMetricDTO> getByExecutionId(UUID executionId) {
        return healingMetricRepository.findByExecutionId(executionId).stream()
                .map(HealingMetricDTO::from)
                .toList();
    }

    public Map<String, Long> getActionTypeBreakdown() {
        Map<String, Long> breakdown = new HashMap<>();
        for (HealingMetricEntity entity : healingMetricRepository.findAll()) {
            breakdown.merge(entity.getActionType(), 1L, Long::sum);
        }
        return breakdown;
    }
}
