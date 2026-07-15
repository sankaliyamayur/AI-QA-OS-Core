package com.aiqaos.dashboard.service;

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

    public HealingAnalyticsService(HealingMetricRepository healingMetricRepository) {
        this.healingMetricRepository = healingMetricRepository;
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
