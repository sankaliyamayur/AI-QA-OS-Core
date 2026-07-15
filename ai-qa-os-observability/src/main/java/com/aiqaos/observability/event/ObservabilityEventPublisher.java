package com.aiqaos.observability.event;

import com.aiqaos.observability.entity.AgentMetricEntity;
import com.aiqaos.observability.entity.BugMetricEntity;
import com.aiqaos.observability.entity.HealingMetricEntity;
import com.aiqaos.observability.entity.TimelineEventEntity;
import com.aiqaos.observability.repository.AgentMetricsRepository;
import com.aiqaos.observability.repository.BugMetricRepository;
import com.aiqaos.observability.repository.HealingMetricRepository;
import com.aiqaos.observability.repository.TimelineEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Synchronously persists Phase 22 analytics entities (guaranteed durability for dashboard
 * queries) and additionally re-publishes each event via Spring's ApplicationEventPublisher
 * for decoupled fan-out (e.g. the SSE live-monitoring broadcaster), reusing the existing
 * ObservabilityEventCollector/EventProcessor chain.
 */
@Component
public class ObservabilityEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ObservabilityEventPublisher.class);

    private final AgentMetricsRepository agentMetricsRepository;
    private final TimelineEventRepository timelineEventRepository;
    private final BugMetricRepository bugMetricRepository;
    private final HealingMetricRepository healingMetricRepository;
    private final ApplicationEventPublisher springEventPublisher;
    private final ObjectMapper objectMapper;

    public ObservabilityEventPublisher(AgentMetricsRepository agentMetricsRepository,
                                        TimelineEventRepository timelineEventRepository,
                                        BugMetricRepository bugMetricRepository,
                                        HealingMetricRepository healingMetricRepository,
                                        ApplicationEventPublisher springEventPublisher,
                                        ObjectMapper objectMapper) {
        this.agentMetricsRepository = agentMetricsRepository;
        this.timelineEventRepository = timelineEventRepository;
        this.bugMetricRepository = bugMetricRepository;
        this.healingMetricRepository = healingMetricRepository;
        this.springEventPublisher = springEventPublisher;
        this.objectMapper = objectMapper;
    }

    public AgentMetricEntity recordAgentMetric(AgentMetricEntity entity) {
        AgentMetricEntity saved = agentMetricsRepository.save(entity);
        publishEnvelope("AGENT_METRIC", entity.getCorrelationId(), saved);
        return saved;
    }

    public TimelineEventEntity recordTimelineEvent(TimelineEventEntity entity) {
        TimelineEventEntity saved = timelineEventRepository.save(entity);
        publishEnvelope("TIMELINE_EVENT", entity.getCorrelationId(), saved);
        return saved;
    }

    public BugMetricEntity recordBugMetric(BugMetricEntity entity) {
        BugMetricEntity saved = bugMetricRepository.save(entity);
        publishEnvelope("BUG_METRIC", null, saved);
        return saved;
    }

    public HealingMetricEntity recordHealingMetric(HealingMetricEntity entity) {
        HealingMetricEntity saved = healingMetricRepository.save(entity);
        publishEnvelope("HEALING_METRIC", null, saved);
        return saved;
    }

    private void publishEnvelope(String eventType, String correlationId, Object payload) {
        ObservabilityEventEnvelope envelope = new ObservabilityEventEnvelope();
        envelope.setEventId(UUID.randomUUID().toString());
        envelope.setEventType(eventType);
        envelope.setSource("observability-event-publisher");
        envelope.setCorrelationId(correlationId);
        envelope.setPayload(toJson(payload));
        springEventPublisher.publishEvent(envelope);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize observability event payload: {}", e.getMessage());
            return String.valueOf(payload);
        }
    }
}
