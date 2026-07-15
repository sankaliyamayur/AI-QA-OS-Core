package com.aiqaos.observability.event;

import com.aiqaos.observability.entity.AgentMetricEntity;
import com.aiqaos.observability.entity.BugMetricEntity;
import com.aiqaos.observability.entity.HealingMetricEntity;
import com.aiqaos.observability.entity.TimelineEventEntity;
import com.aiqaos.observability.repository.AgentMetricsRepository;
import com.aiqaos.observability.repository.BugMetricRepository;
import com.aiqaos.observability.repository.HealingMetricRepository;
import com.aiqaos.observability.repository.TimelineEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ObservabilityEventPublisherTest {

    private AgentMetricsRepository agentMetricsRepository;
    private TimelineEventRepository timelineEventRepository;
    private BugMetricRepository bugMetricRepository;
    private HealingMetricRepository healingMetricRepository;
    private ApplicationEventPublisher springEventPublisher;
    private ObservabilityEventPublisher publisher;

    @BeforeEach
    void setUp() {
        agentMetricsRepository = mock(AgentMetricsRepository.class);
        timelineEventRepository = mock(TimelineEventRepository.class);
        bugMetricRepository = mock(BugMetricRepository.class);
        healingMetricRepository = mock(HealingMetricRepository.class);
        springEventPublisher = mock(ApplicationEventPublisher.class);

        when(agentMetricsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(timelineEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(bugMetricRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(healingMetricRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        publisher = new ObservabilityEventPublisher(agentMetricsRepository, timelineEventRepository,
                bugMetricRepository, healingMetricRepository, springEventPublisher, new ObjectMapper());
    }

    @Test
    void recordAgentMetricSavesAndPublishesEnvelope() {
        AgentMetricEntity entity = new AgentMetricEntity();
        entity.setCorrelationId("corr-1");
        entity.setAgentType("QAAnalysisStep");

        AgentMetricEntity saved = publisher.recordAgentMetric(entity);

        assertNotNull(saved);
        verify(agentMetricsRepository).save(entity);
        verify(springEventPublisher).publishEvent(any(ObservabilityEventEnvelope.class));
    }

    @Test
    void recordTimelineEventSavesAndPublishesEnvelope() {
        TimelineEventEntity entity = new TimelineEventEntity();
        entity.setCorrelationId("corr-2");
        entity.setEventType("STEP_STARTED");

        publisher.recordTimelineEvent(entity);

        verify(timelineEventRepository).save(entity);
        verify(springEventPublisher).publishEvent(any(ObservabilityEventEnvelope.class));
    }

    @Test
    void recordBugMetricSavesAndPublishesEnvelope() {
        BugMetricEntity entity = new BugMetricEntity();
        entity.setFailureCategory("ELEMENT_NOT_FOUND");

        publisher.recordBugMetric(entity);

        verify(bugMetricRepository).save(entity);
        verify(springEventPublisher).publishEvent(any(ObservabilityEventEnvelope.class));
    }

    @Test
    void recordHealingMetricSavesAndPublishesEnvelope() {
        HealingMetricEntity entity = new HealingMetricEntity();
        entity.setActionType("LOCATOR_UPDATE");

        publisher.recordHealingMetric(entity);

        verify(healingMetricRepository).save(entity);
        verify(springEventPublisher).publishEvent(any(ObservabilityEventEnvelope.class));
    }

    @Test
    void envelopePayloadIsValidJsonForSavedEntity() {
        var captor = org.mockito.ArgumentCaptor.forClass(ObservabilityEventEnvelope.class);

        AgentMetricEntity entity = new AgentMetricEntity();
        entity.setCorrelationId("corr-3");
        entity.setAgentType("ScriptGenerationStep");
        publisher.recordAgentMetric(entity);

        verify(springEventPublisher).publishEvent(captor.capture());
        ObservabilityEventEnvelope envelope = captor.getValue();
        assertEquals("AGENT_METRIC", envelope.getEventType());
        assertEquals("corr-3", envelope.getCorrelationId());
        assertNotNull(envelope.getPayload());
    }
}
