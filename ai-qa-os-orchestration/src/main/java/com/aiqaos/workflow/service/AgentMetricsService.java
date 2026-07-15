package com.aiqaos.workflow.service;

import com.aiqaos.observability.entity.AgentMetricEntity;
import com.aiqaos.observability.event.ObservabilityEventPublisher;
import com.aiqaos.observability.repository.AgentMetricsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Thin facade over the observability module's ObservabilityEventPublisher/AgentMetricsRepository -
 * orchestration does not own the agent_metrics table, it only produces the data.
 */
@Service
public class AgentMetricsService {

    private final ObservabilityEventPublisher observabilityEventPublisher;
    private final AgentMetricsRepository agentMetricsRepository;

    public AgentMetricsService(ObservabilityEventPublisher observabilityEventPublisher,
                                AgentMetricsRepository agentMetricsRepository) {
        this.observabilityEventPublisher = observabilityEventPublisher;
        this.agentMetricsRepository = agentMetricsRepository;
    }

    public void recordStepMetric(UUID executionId,
                                  UUID workflowId,
                                  String agentType,
                                  String operation,
                                  String correlationId,
                                  long durationMs,
                                  Long tokensUsed,
                                  Double cost,
                                  boolean success,
                                  String errorMessage) {
        AgentMetricEntity entity = new AgentMetricEntity();
        entity.setExecutionId(executionId);
        entity.setWorkflowId(workflowId);
        entity.setAgentType(agentType);
        entity.setOperation(operation);
        entity.setCorrelationId(correlationId);
        entity.setDurationMs(durationMs);
        entity.setTokensUsed(tokensUsed);
        entity.setCost(cost);
        entity.setSuccess(success);
        entity.setErrorMessage(errorMessage);
        entity.setRecordedAt(LocalDateTime.now());
        observabilityEventPublisher.recordAgentMetric(entity);
    }

    public List<AgentMetricEntity> getByExecutionId(UUID executionId) {
        return agentMetricsRepository.findByExecutionId(executionId);
    }
}
