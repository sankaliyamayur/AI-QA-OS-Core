package com.aiqaos.workflow.service;

import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.observability.entity.BugMetricEntity;
import com.aiqaos.observability.event.ObservabilityEventPublisher;
import com.aiqaos.observability.repository.BugMetricRepository;
import com.aiqaos.workflow.model.BugAnalysisDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BugAnalyticsService {

    private final ObservabilityEventPublisher observabilityEventPublisher;
    private final BugMetricRepository bugMetricRepository;

    public BugAnalyticsService(ObservabilityEventPublisher observabilityEventPublisher,
                                BugMetricRepository bugMetricRepository) {
        this.observabilityEventPublisher = observabilityEventPublisher;
        this.bugMetricRepository = bugMetricRepository;
    }

    public void recordBug(UUID executionId, UUID workflowId, BugAnalysisReport report) {
        if (report == null || "NONE".equals(report.getFailureCategory())) {
            return;
        }
        BugMetricEntity entity = new BugMetricEntity();
        entity.setExecutionId(executionId);
        entity.setWorkflowId(workflowId);
        entity.setBugReportId(report.getReportId());
        entity.setFailureCategory(report.getFailureCategory());
        entity.setSeverity(report.getSeverity());
        entity.setPriority(report.getPriority());
        entity.setRootCause(report.getRootCause());
        entity.setConfidence(report.getConfidence());
        entity.setAutoDetected(true);
        entity.setStatus(report.getStatus());
        entity.setDetectedAt(report.getCreatedTime() != null ? report.getCreatedTime() : LocalDateTime.now());
        observabilityEventPublisher.recordBugMetric(entity);
    }

    public List<BugAnalysisDTO> getByExecutionId(UUID executionId) {
        return bugMetricRepository.findByExecutionId(executionId).stream()
                .map(BugAnalyticsService::toDTO)
                .toList();
    }

    public Map<String, Long> getFailureCategoryBreakdown() {
        Map<String, Long> breakdown = new HashMap<>();
        for (BugMetricEntity entity : bugMetricRepository.findAll()) {
            breakdown.merge(entity.getFailureCategory(), 1L, Long::sum);
        }
        return breakdown;
    }

    private static BugAnalysisDTO toDTO(BugMetricEntity entity) {
        BugAnalysisDTO dto = new BugAnalysisDTO();
        dto.setId(entity.getId());
        dto.setExecutionId(entity.getExecutionId());
        dto.setBugReportId(entity.getBugReportId());
        dto.setFailureCategory(entity.getFailureCategory());
        dto.setSeverity(entity.getSeverity());
        dto.setPriority(entity.getPriority());
        dto.setRootCause(entity.getRootCause());
        dto.setConfidence(entity.getConfidence());
        dto.setAutoDetected(entity.isAutoDetected());
        dto.setStatus(entity.getStatus());
        dto.setDetectedAt(entity.getDetectedAt());
        return dto;
    }
}
