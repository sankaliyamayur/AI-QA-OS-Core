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

    @Autowired
    private com.aiqaos.workflow.pipeline.AutonomousQAPipelineOrchestrator orchestrator;

    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        return orchestrator.runPipeline(request, context);
    }
}