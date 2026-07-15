package com.aiqaos.workflow.service;

import com.aiqaos.observability.entity.TimelineEventEntity;
import com.aiqaos.observability.event.ObservabilityEventPublisher;
import com.aiqaos.observability.repository.TimelineEventRepository;
import com.aiqaos.workflow.model.TimelineEventDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TimelineService {

    private final ObservabilityEventPublisher observabilityEventPublisher;
    private final TimelineEventRepository timelineEventRepository;

    public TimelineService(ObservabilityEventPublisher observabilityEventPublisher,
                            TimelineEventRepository timelineEventRepository) {
        this.observabilityEventPublisher = observabilityEventPublisher;
        this.timelineEventRepository = timelineEventRepository;
    }

    public void recordEvent(UUID executionId,
                             UUID workflowId,
                             String correlationId,
                             String eventType,
                             String stepName,
                             String description,
                             String status,
                             Long durationMs) {
        int nextSequence = timelineEventRepository.countByExecutionId(executionId) + 1;

        TimelineEventEntity entity = new TimelineEventEntity();
        entity.setExecutionId(executionId);
        entity.setWorkflowId(workflowId);
        entity.setCorrelationId(correlationId);
        entity.setSequenceNumber(nextSequence);
        entity.setEventType(eventType);
        entity.setStepName(stepName);
        entity.setDescription(description);
        entity.setStatus(status);
        entity.setDurationMs(durationMs);
        entity.setOccurredAt(LocalDateTime.now());
        observabilityEventPublisher.recordTimelineEvent(entity);
    }

    public List<TimelineEventDTO> getTimeline(UUID executionId) {
        return timelineEventRepository.findByExecutionIdOrderBySequenceNumberAsc(executionId)
                .stream()
                .map(TimelineService::toDTO)
                .toList();
    }

    private static TimelineEventDTO toDTO(TimelineEventEntity entity) {
        TimelineEventDTO dto = new TimelineEventDTO();
        dto.setId(entity.getId());
        dto.setExecutionId(entity.getExecutionId());
        dto.setSequenceNumber(entity.getSequenceNumber());
        dto.setEventType(entity.getEventType());
        dto.setStepName(entity.getStepName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setDurationMs(entity.getDurationMs());
        dto.setOccurredAt(entity.getOccurredAt());
        return dto;
    }
}
