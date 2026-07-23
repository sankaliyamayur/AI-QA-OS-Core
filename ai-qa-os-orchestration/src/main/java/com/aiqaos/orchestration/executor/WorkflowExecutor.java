package com.aiqaos.orchestration.executor;

import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.orchestration.dsl.WorkflowDefinition;
import com.aiqaos.orchestration.graph.WorkflowNode;
import com.aiqaos.orchestration.context.WorkflowVariables;
import com.aiqaos.orchestration.model.WorkflowStepResultDTO;
import com.aiqaos.orchestration.component.WorkflowResultAggregator;
import com.aiqaos.orchestration.component.WorkflowEventPublisher;
import com.aiqaos.orchestration.event.WorkflowStartedEvent;
import com.aiqaos.orchestration.event.WorkflowCompletedEvent;
import com.aiqaos.orchestration.event.WorkflowFailedEvent;
import com.aiqaos.orchestration.recovery.WorkflowRecoveryManager;
import com.aiqaos.orchestration.registry.WorkflowRegistry;
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
    private com.aiqaos.orchestration.pipeline.AutonomousQAPipelineOrchestrator orchestrator;

    public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
        return orchestrator.runPipeline(request, context);
    }

}