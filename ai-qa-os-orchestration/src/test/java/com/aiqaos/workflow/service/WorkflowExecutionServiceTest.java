package com.aiqaos.workflow.service;

import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.observability.entity.LLMCostEntity;
import com.aiqaos.observability.repository.LLMCostRepository;
import com.aiqaos.workflow.entity.WorkflowExecutionEntity;
import com.aiqaos.workflow.repository.WorkflowExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowExecutionServiceTest {

    private final Map<UUID, WorkflowExecutionEntity> store = new HashMap<>();
    private WorkflowExecutionRepository workflowExecutionRepository;
    private LLMCostRepository llmCostRepository;
    private WorkflowExecutionService service;

    @BeforeEach
    void setUp() {
        workflowExecutionRepository = mock(WorkflowExecutionRepository.class);
        when(workflowExecutionRepository.save(any())).thenAnswer(inv -> {
            WorkflowExecutionEntity entity = inv.getArgument(0);
            store.put(entity.getExecutionId(), entity);
            return entity;
        });
        when(workflowExecutionRepository.findByExecutionId(any()))
                .thenAnswer(inv -> Optional.ofNullable(store.get(inv.getArgument(0))));

        llmCostRepository = mock(LLMCostRepository.class);
        service = new WorkflowExecutionService(workflowExecutionRepository, llmCostRepository);
    }

    @Test
    void startExecutionPopulatesProvenanceFieldsFromInputs() {
        WorkflowRequest request = new WorkflowRequest();
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("gitCommit", "abc123");
        inputs.put("gitBranch", "main");
        inputs.put("llmModel", "gpt-4o-mini");
        inputs.put("pipelineVersion", "v1.2.0");
        inputs.put("environment", "DEV");
        inputs.put("browser", "CHROME");
        request.setInputs(inputs);

        WorkflowContext context = new WorkflowContext();

        WorkflowExecutionEntity entity = service.startExecution(request, context);

        assertNotNull(entity.getExecutionId());
        assertEquals("RUNNING", entity.getStatus());
        assertEquals("abc123", entity.getGitCommit());
        assertEquals("main", entity.getGitBranch());
        assertEquals("gpt-4o-mini", entity.getLlmModel());
        assertEquals("v1.2.0", entity.getPipelineVersion());
        assertEquals("DEV", entity.getEnvironment());
        assertEquals("CHROME", entity.getBrowser());

        // context metadata should be back-filled with the generated execution/workflow ids
        assertEquals(entity.getExecutionId(), context.getMetadata().getExecutionId());
        assertEquals(entity.getWorkflowId(), context.getMetadata().getWorkflowId());
    }

    @Test
    void completeExecutionAggregatesCostAndTokensFromLlmCostRepository() {
        WorkflowRequest request = new WorkflowRequest();
        request.setInputs(new HashMap<>());
        WorkflowContext context = new WorkflowContext();
        context.getMetadata().setCorrelationId(UUID.randomUUID());

        WorkflowExecutionEntity started = service.startExecution(request, context);

        LLMCostEntity cost1 = new LLMCostEntity();
        cost1.setInputTokens(100);
        cost1.setOutputTokens(50);
        cost1.setCost(0.01);
        LLMCostEntity cost2 = new LLMCostEntity();
        cost2.setInputTokens(200);
        cost2.setOutputTokens(100);
        cost2.setCost(0.02);
        when(llmCostRepository.findByRequestId(anyString())).thenReturn(List.of(cost1, cost2));

        WorkflowResponse response = new WorkflowResponse();
        response.setStatus("COMPLETED");
        response.setMessage("Pipeline executed successfully");

        WorkflowExecutionEntity completed = service.completeExecution(
                started.getExecutionId(), response, context, 9, 9, 0, 0, 0);

        assertEquals("COMPLETED", completed.getStatus());
        assertNotNull(completed.getEndTime());
        assertEquals(450L, completed.getTokenUsage());
        assertEquals(0.03, completed.getExecutionCost(), 0.0001);
    }

    @Test
    void compareComputesDeltasBetweenTwoExecutions() {
        WorkflowExecutionEntity a = new WorkflowExecutionEntity();
        a.setExecutionId(UUID.randomUUID());
        a.setStatus("COMPLETED");
        a.setDuration(1000);
        a.setExecutionCost(0.10);
        a.setTokenUsage(500);
        a.setFailedSteps(0);
        store.put(a.getExecutionId(), a);

        WorkflowExecutionEntity b = new WorkflowExecutionEntity();
        b.setExecutionId(UUID.randomUUID());
        b.setStatus("FAILED");
        b.setDuration(1800);
        b.setExecutionCost(0.15);
        b.setTokenUsage(700);
        b.setFailedSteps(1);
        store.put(b.getExecutionId(), b);

        var comparison = service.compare(a.getExecutionId(), b.getExecutionId());

        assertEquals(800L, comparison.getDurationDeltaMs());
        assertEquals(0.05, comparison.getCostDelta(), 0.0001);
        assertEquals(200L, comparison.getTokenDelta());
        assertEquals(1, comparison.getFailedStepsDelta());
        assertEquals(1, comparison.getNotes().size());
    }
}
