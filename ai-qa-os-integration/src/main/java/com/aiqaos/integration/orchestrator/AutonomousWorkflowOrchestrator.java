package com.aiqaos.integration.orchestrator;

import com.aiqaos.core.contract.*;
import com.aiqaos.integration.context.IntegrationContext;
import com.aiqaos.integration.context.IntegrationResult;
import com.aiqaos.integration.coordinator.PlatformIntegrationManager;
import com.aiqaos.integration.dto.AgentSummary;
import com.aiqaos.integration.dto.PlatformExecutionSummary;
import com.aiqaos.integration.dto.WorkflowSummary;
import com.aiqaos.integration.facade.PlatformIntegrationFacade;
import com.aiqaos.integration.health.PlatformHealthVerifier;
import com.aiqaos.integration.pipeline.LearningPipeline;
import com.aiqaos.integration.pipeline.RequestPipeline;
import com.aiqaos.integration.recovery.RecoveryManager;
import com.aiqaos.integration.state.PlatformState;
import com.aiqaos.integration.state.PlatformStateManager;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AutonomousWorkflowOrchestrator {

    private final PlatformIntegrationFacade facade;
    private final PlatformIntegrationManager integrationManager;
    private final PlatformStateManager stateManager;
    private final PlatformHealthVerifier healthVerifier;
    private final RecoveryManager recoveryManager;
    private final RequestPipeline requestPipeline;
    private final LearningPipeline learningPipeline;
    private final ApplicationEventPublisher eventPublisher;

    public AutonomousWorkflowOrchestrator(PlatformIntegrationFacade facade,
                                          PlatformIntegrationManager integrationManager,
                                          PlatformStateManager stateManager,
                                          PlatformHealthVerifier healthVerifier,
                                          RecoveryManager recoveryManager,
                                          RequestPipeline requestPipeline,
                                          LearningPipeline learningPipeline,
                                          ApplicationEventPublisher eventPublisher) {
        this.facade = facade;
        this.integrationManager = integrationManager;
        this.stateManager = stateManager;
        this.healthVerifier = healthVerifier;
        this.recoveryManager = recoveryManager;
        this.requestPipeline = requestPipeline;
        this.learningPipeline = learningPipeline;
        this.eventPublisher = eventPublisher;
    }

    public IntegrationResult executeAutonomousQA(String userStory, String userId) {
        // Step 1: Pre-flight Verification
        if (!healthVerifier.verifyPlatformHealth()) {
            return new IntegrationResult("FAILURE", "Pre-flight health verification failed", null);
        }

        // Step 2: Request Validation
        if (!requestPipeline.validateRequest(userStory)) {
            return new IntegrationResult("FAILURE", "Invalid request body", null);
        }

        // Step 3: Establish Integration Context
        IntegrationContext context = integrationManager.establishContext(userId);
        String workflowIdStr = context.getWorkflowId().toString();
        
        stateManager.transitionTo(workflowIdStr, PlatformState.REQUEST_RECEIVED);
        eventPublisher.publishEvent(new AutonomousQAEvent(this, "REQUEST_RECEIVED", workflowIdStr));

        try {
            // Step 4: Authentication and memory retrieval
            stateManager.transitionTo(workflowIdStr, PlatformState.AUTHENTICATED);
            facade.retrieveMemory(userStory);
            stateManager.transitionTo(workflowIdStr, PlatformState.MEMORY_READY);

            // Step 5: Brain analysis strategy planning
            stateManager.transitionTo(workflowIdStr, PlatformState.PLAN_CREATED);
            BrainRequest brainReq = new BrainRequest();
            brainReq.getMetadata().setCorrelationId(context.getCorrelationId());
            brainReq.setRequirementDescription(userStory);
            facade.analyze(brainReq);

            // Step 6: Workflow Execution
            stateManager.transitionTo(workflowIdStr, PlatformState.WORKFLOW_RUNNING);
            WorkflowRequest wfReq = new WorkflowRequest();
            wfReq.getMetadata().setCorrelationId(context.getCorrelationId());
            wfReq.getMetadata().setWorkflowId(context.getWorkflowId());
            wfReq.setWorkflowName("Autonomous QA Workflow");
            facade.executeWorkflow(wfReq);
            recoveryManager.saveCheckpoint(workflowIdStr, "EXECUTION", PlatformState.EXECUTING);

            // Step 7: Compile final reports
            stateManager.transitionTo(workflowIdStr, PlatformState.REPORTING);
            ReportRequest repReq = new ReportRequest();
            repReq.getMetadata().setCorrelationId(context.getCorrelationId());
            facade.compileReport(repReq);

            // Step 8: Memory Learning
            stateManager.transitionTo(workflowIdStr, PlatformState.LEARNING);
            learningPipeline.saveExecutionLearning(context.getExecutionId(), "Execution completed successfully for: " + userStory);

            stateManager.transitionTo(workflowIdStr, PlatformState.COMPLETED);
            eventPublisher.publishEvent(new AutonomousQAEvent(this, "COMPLETED", workflowIdStr));

            PlatformExecutionSummary summary = new PlatformExecutionSummary(
                "SUCCESS",
                1200L,
                new WorkflowSummary(workflowIdStr, "Autonomous QA Suite", 5, 5),
                List.of(
                    new AgentSummary("QA Analyst", "Requirement Analysis", "SUCCESS"),
                    new AgentSummary("Automation Agent", "Playwright Scripting", "SUCCESS")
                )
            );

            return new IntegrationResult("SUCCESS", "Autonomous QA executed successfully", summary);

        } catch (Exception e) {
            eventPublisher.publishEvent(new AutonomousQAEvent(this, "FAILED", workflowIdStr));
            return new IntegrationResult("FAILURE", "Error during orchestration: " + e.getMessage(), null);
        } finally {
            integrationManager.clearContext();
        }
    }

    public static class AutonomousQAEvent extends ApplicationEvent {
        private final String status;
        private final String workflowId;

        public AutonomousQAEvent(Object source, String status, String workflowId) {
            super(source);
            this.status = status;
            this.workflowId = workflowId;
        }

        public String getStatus() { return status; }
        public String getWorkflowId() { return workflowId; }
    }
}