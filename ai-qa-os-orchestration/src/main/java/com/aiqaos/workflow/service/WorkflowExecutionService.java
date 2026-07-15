package com.aiqaos.workflow.service;

import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.observability.entity.LLMCostEntity;
import com.aiqaos.observability.repository.LLMCostRepository;
import com.aiqaos.workflow.entity.WorkflowExecutionEntity;
import com.aiqaos.workflow.model.ExecutionComparisonDTO;
import com.aiqaos.workflow.model.ExecutionHistoryDTO;
import com.aiqaos.workflow.repository.WorkflowExecutionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * Owns the persistence lifecycle for WorkflowExecutionEntity. This is the first real
 * save-path for workflow_executions rows - no prior code in the repo persisted them.
 */
@Service
public class WorkflowExecutionService {

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final LLMCostRepository llmCostRepository;

    public WorkflowExecutionService(WorkflowExecutionRepository workflowExecutionRepository,
                                     LLMCostRepository llmCostRepository) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.llmCostRepository = llmCostRepository;
    }

    public WorkflowExecutionEntity startExecution(WorkflowRequest request, WorkflowContext context) {
        UUID executionId = context.getMetadata().getExecutionId();
        if (executionId == null) {
            executionId = UUID.randomUUID();
            context.getMetadata().setExecutionId(executionId);
        }
        UUID workflowId = context.getMetadata().getWorkflowId();
        if (workflowId == null) {
            workflowId = UUID.randomUUID();
            context.getMetadata().setWorkflowId(workflowId);
        }

        WorkflowExecutionEntity entity = new WorkflowExecutionEntity();
        entity.setExecutionId(executionId);
        entity.setWorkflowId(workflowId);
        entity.setStatus("RUNNING");
        entity.setStartTime(LocalDateTime.now());

        var inputs = request.getInputs();
        if (inputs != null) {
            entity.setGitCommit(asString(inputs.get("gitCommit")));
            entity.setGitBranch(asString(inputs.get("gitBranch")));
            entity.setLlmModel(asString(inputs.get("llmModel")));
            entity.setPipelineVersion(asString(inputs.get("pipelineVersion")));
            entity.setEnvironment(asString(inputs.get("environment")));
            entity.setBrowser(asString(inputs.get("browser")));
        }

        return workflowExecutionRepository.save(entity);
    }

    public void updateCurrentStep(UUID executionId, String stepName) {
        workflowExecutionRepository.findByExecutionId(executionId).ifPresent(entity -> {
            entity.setCurrentStep(stepName);
            workflowExecutionRepository.save(entity);
        });
    }

    public WorkflowExecutionEntity completeExecution(UUID executionId,
                                                       WorkflowResponse response,
                                                       WorkflowContext context,
                                                       int totalSteps,
                                                       int successSteps,
                                                       int failedSteps,
                                                       int skippedSteps,
                                                       int retryCount) {
        WorkflowExecutionEntity entity = workflowExecutionRepository.findByExecutionId(executionId)
                .orElseThrow(() -> new NoSuchElementException("WorkflowExecutionEntity not found for executionId=" + executionId));

        LocalDateTime endTime = LocalDateTime.now();
        entity.setEndTime(endTime);
        if (entity.getStartTime() != null) {
            entity.setDuration(java.time.Duration.between(entity.getStartTime(), endTime).toMillis());
        }
        entity.setStatus(response.getStatus());
        entity.setResult(response.getMessage());
        entity.setTotalSteps(totalSteps);
        entity.setSuccessSteps(successSteps);
        entity.setFailedSteps(failedSteps);
        entity.setSkippedSteps(skippedSteps);
        entity.setRetryCount(retryCount);
        entity.setCurrentStep(null);

        UUID correlationId = context.getMetadata().getCorrelationId();
        if (correlationId != null) {
            var costRows = llmCostRepository.findByRequestId(correlationId.toString());
            double totalCost = costRows.stream().mapToDouble(LLMCostEntity::getCost).sum();
            long totalTokens = costRows.stream()
                    .mapToLong(c -> c.getInputTokens() + c.getOutputTokens())
                    .sum();
            entity.setExecutionCost(totalCost);
            entity.setTokenUsage(totalTokens);
        }

        return workflowExecutionRepository.save(entity);
    }

    public Page<ExecutionHistoryDTO> search(String status, String gitBranch, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return workflowExecutionRepository.search(status, gitBranch, from, to, pageable)
                .map(ExecutionHistoryDTO::from);
    }

    public Optional<ExecutionHistoryDTO> getByExecutionId(UUID executionId) {
        return workflowExecutionRepository.findByExecutionId(executionId).map(ExecutionHistoryDTO::from);
    }

    public ExecutionComparisonDTO compare(UUID executionIdA, UUID executionIdB) {
        WorkflowExecutionEntity entityA = workflowExecutionRepository.findByExecutionId(executionIdA)
                .orElseThrow(() -> new NoSuchElementException("Execution not found: " + executionIdA));
        WorkflowExecutionEntity entityB = workflowExecutionRepository.findByExecutionId(executionIdB)
                .orElseThrow(() -> new NoSuchElementException("Execution not found: " + executionIdB));

        ExecutionComparisonDTO dto = new ExecutionComparisonDTO();
        dto.setExecutionA(ExecutionHistoryDTO.from(entityA));
        dto.setExecutionB(ExecutionHistoryDTO.from(entityB));
        dto.setDurationDeltaMs(entityB.getDuration() - entityA.getDuration());
        dto.setCostDelta(entityB.getExecutionCost() - entityA.getExecutionCost());
        dto.setTokenDelta(entityB.getTokenUsage() - entityA.getTokenUsage());
        dto.setFailedStepsDelta(entityB.getFailedSteps() - entityA.getFailedSteps());

        if (!java.util.Objects.equals(entityA.getStatus(), entityB.getStatus())) {
            dto.getNotes().add("Status changed from " + entityA.getStatus() + " to " + entityB.getStatus());
        }
        if (!java.util.Objects.equals(entityA.getGitBranch(), entityB.getGitBranch())) {
            dto.getNotes().add("Branch changed from " + entityA.getGitBranch() + " to " + entityB.getGitBranch());
        }
        return dto;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
