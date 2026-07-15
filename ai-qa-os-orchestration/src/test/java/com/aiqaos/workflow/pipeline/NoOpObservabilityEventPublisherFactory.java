package com.aiqaos.workflow.pipeline;

import com.aiqaos.observability.event.ObservabilityEventPublisher;
import com.aiqaos.observability.repository.AgentMetricsRepository;
import com.aiqaos.observability.repository.BugMetricRepository;
import com.aiqaos.observability.repository.HealingMetricRepository;
import com.aiqaos.observability.repository.TimelineEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Builds an ObservabilityEventPublisher backed by pass-through Mockito repository doubles, for
 * tests that construct SelfHealingEngineImpl/AutonomousQAPipelineOrchestrator directly and only
 * need the publisher to not NPE (no assertions on the persisted rows here - see
 * AutonomousQAPipelineTest for the full event-flow integration check).
 */
final class NoOpObservabilityEventPublisherFactory {

    private NoOpObservabilityEventPublisherFactory() {
    }

    static ObservabilityEventPublisher create() {
        AgentMetricsRepository agentMetricsRepository = mock(AgentMetricsRepository.class);
        when(agentMetricsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TimelineEventRepository timelineEventRepository = mock(TimelineEventRepository.class);
        when(timelineEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(timelineEventRepository.countByExecutionId(any())).thenReturn(0);

        BugMetricRepository bugMetricRepository = mock(BugMetricRepository.class);
        when(bugMetricRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HealingMetricRepository healingMetricRepository = mock(HealingMetricRepository.class);
        when(healingMetricRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApplicationEventPublisher springEventPublisher = mock(ApplicationEventPublisher.class);

        return new ObservabilityEventPublisher(agentMetricsRepository, timelineEventRepository,
                bugMetricRepository, healingMetricRepository, springEventPublisher, new ObjectMapper());
    }
}
