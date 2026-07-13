package com.aiqaos.workflow.executor;

import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.workflow.dsl.WorkflowDefinition;
import com.aiqaos.workflow.graph.WorkflowNode;
import com.aiqaos.workflow.context.WorkflowVariables;
import com.aiqaos.workflow.model.WorkflowStepResultDTO;
import com.aiqaos.workflow.component.WorkflowResultAggregator;
import com.aiqaos.workflow.component.WorkflowEventPublisher;
import com.aiqaos.workflow.event.WorkflowStartedEvent;
import com.aiqaos.workflow.event.WorkflowCompletedEvent;
import com.aiqaos.workflow.event.WorkflowFailedEvent;
import com.aiqaos.workflow.recovery.WorkflowRecoveryManager;
import com.aiqaos.workflow.registry.WorkflowRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class WorkflowExecutor {

    @Autowired
    private WorkflowRegistry registry;

    @Autowired
    private WorkflowStepExecutor stepExecutor;

    @Autowired
    private WorkflowResultAggregator aggregator;

    @Autowired
    private WorkflowEventPublisher eventPublisher;

    @Autowired
    private WorkflowRecoveryManager recoveryManager;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        UUID executionId = UUID.randomUUID();
        UUID workflowId = request.getMetadata().getWorkflowId() != null ? 
            request.getMetadata().getWorkflowId() : UUID.randomUUID();

        eventPublisher.publish(new WorkflowStartedEvent(workflowId));

        String lookupKey = request.getMetadata().getWorkflowId() != null ? 
            request.getMetadata().getWorkflowId().toString() : request.getWorkflowName();

        WorkflowDefinition def = registry.getWorkflow(lookupKey);
        if (def == null) {
            def = new WorkflowDefinition();
            def.setId(lookupKey);
            def.setName(request.getWorkflowName() != null ? request.getWorkflowName() : "Fallback Workflow");
            
            WorkflowNode node = new WorkflowNode();
            node.setStepId("step1");
            node.setAgentType("QA_ANALYST");
            node.setExecutionOrder(1);
            def.getSteps().add(node);
        }

        WorkflowVariables vars = new WorkflowVariables();
        if (request.getInputs() != null) {
            request.getInputs().forEach(vars::set);
        }

        List<WorkflowStepResultDTO> stepResults = new ArrayList<>();
        boolean failed = false;

        List<WorkflowNode> steps = def.getSteps();
        
        for (WorkflowNode node : steps) {
            if (node.isParallel()) {
                CompletableFuture<WorkflowStepResultDTO> future = CompletableFuture.supplyAsync(
                    () -> stepExecutor.executeStep(node, vars), executorService
                );
                try {
                    WorkflowStepResultDTO r = future.get();
                    stepResults.add(r);
                    if ("FAILED".equals(r.getStatus())) {
                        failed = true;
                        break;
                    }
                } catch (Exception e) {
                    failed = true;
                    break;
                }
            } else {
                WorkflowStepResultDTO r = stepExecutor.executeStep(node, vars);
                stepResults.add(r);
                if ("FAILED".equals(r.getStatus())) {
                    failed = true;
                    break;
                }
            }
        }

        WorkflowResponse response = new WorkflowResponse();
        response.getMetadata().setWorkflowId(workflowId);
        response.getMetadata().setExecutionId(executionId);

        if (failed) {
            eventPublisher.publish(new WorkflowFailedEvent(workflowId, "A step execution failed."));
            response.setStatus("FAILED");
            response.setMessage("Workflow failed execution.");
        } else {
            eventPublisher.publish(new WorkflowCompletedEvent(workflowId));
            response.setStatus("SUCCESS");
            response.setMessage("Workflow completed successfully.");
        }

        return response;
    }
}