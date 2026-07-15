package com.aiqaos.dashboard.service;

import com.aiqaos.dashboard.dto.AgentTraceDetailDTO;
import com.aiqaos.dashboard.dto.AgentTraceSummaryDTO;
import com.aiqaos.observability.entity.TimelineEventEntity;
import com.aiqaos.observability.repository.AgentTraceRepository;
import com.aiqaos.observability.repository.TimelineEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Agent traces (full LLM prompt/response history) only carry correlationId - they have no
 * direct executionId column. To list traces "by execution", we resolve execution -> the set
 * of correlationIds recorded against it via timeline_events (which does carry both).
 */
@Service
public class AgentTraceService {

    private final AgentTraceRepository agentTraceRepository;
    private final TimelineEventRepository timelineEventRepository;

    public AgentTraceService(AgentTraceRepository agentTraceRepository,
                              TimelineEventRepository timelineEventRepository) {
        this.agentTraceRepository = agentTraceRepository;
        this.timelineEventRepository = timelineEventRepository;
    }

    public List<AgentTraceSummaryDTO> getByCorrelationId(String correlationId) {
        return agentTraceRepository.findByCorrelationId(correlationId).stream()
                .map(AgentTraceSummaryDTO::from)
                .toList();
    }

    public List<AgentTraceSummaryDTO> getByExecutionId(UUID executionId) {
        List<String> correlationIds = timelineEventRepository.findByExecutionIdOrderBySequenceNumberAsc(executionId)
                .stream()
                .map(TimelineEventEntity::getCorrelationId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (correlationIds.isEmpty()) {
            return List.of();
        }
        return agentTraceRepository.findByCorrelationIdIn(correlationIds).stream()
                .map(AgentTraceSummaryDTO::from)
                .toList();
    }

    public AgentTraceDetailDTO getDetail(UUID id) {
        return agentTraceRepository.findById(id)
                .map(AgentTraceDetailDTO::from)
                .orElseThrow(() -> new NoSuchElementException("Agent trace not found: " + id));
    }

    public List<AgentTraceSummaryDTO> getRecent() {
        return agentTraceRepository.findTop50ByOrderByTimestampDesc().stream()
                .map(AgentTraceSummaryDTO::from)
                .toList();
    }
}
