package com.aiqaos.workflow.pipeline;

import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.enums.WorkflowStatus;
import com.aiqaos.workflow.entity.WorkflowExecutionEntity;
import com.aiqaos.workflow.service.AgentMetricsService;
import com.aiqaos.workflow.service.BugAnalyticsService;
import com.aiqaos.workflow.service.TimelineService;
import com.aiqaos.workflow.service.WorkflowExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class AutonomousQAPipelineOrchestrator {

    private final List<com.aiqaos.core.engine.WorkflowStep<WorkflowRequest, WorkflowResponse>> pipelineSteps = new ArrayList<>();

    private final WorkflowExecutionService workflowExecutionService;
    private final TimelineService timelineService;
    private final AgentMetricsService agentMetricsService;
    private final BugAnalyticsService bugAnalyticsService;

    @Autowired
    public AutonomousQAPipelineOrchestrator(
            RequirementReaderStep requirementReaderStep,
            QAAnalysisStep qaAnalysisStep,
            TestCaseGenerationStep testCaseGenerationStep,
            ScriptGenerationStep scriptGenerationStep,
            ExecutionStep executionStep,
            BugAnalysisStep bugAnalysisStep,
            ReportingStep reportingStep,
            LearningStep learningStep,
            SelfHealingStep selfHealingStep,
            WorkflowExecutionService workflowExecutionService,
            TimelineService timelineService,
            AgentMetricsService agentMetricsService,
            BugAnalyticsService bugAnalyticsService) {

        pipelineSteps.add(requirementReaderStep);
        pipelineSteps.add(qaAnalysisStep);
        pipelineSteps.add(testCaseGenerationStep);
        pipelineSteps.add(scriptGenerationStep);
        pipelineSteps.add(executionStep);
        pipelineSteps.add(bugAnalysisStep);
        pipelineSteps.add(reportingStep);
        pipelineSteps.add(learningStep);
        pipelineSteps.add(selfHealingStep);

        this.workflowExecutionService = workflowExecutionService;
        this.timelineService = timelineService;
        this.agentMetricsService = agentMetricsService;
        this.bugAnalyticsService = bugAnalyticsService;
    }

    public WorkflowResponse runPipeline(WorkflowRequest request, WorkflowContext context) {
        context.setStatus(WorkflowStatus.RUNNING);
        WorkflowResponse finalResponse = new WorkflowResponse();
        finalResponse.setStatus("SUCCESS");
        finalResponse.setMessage("Pipeline executed successfully");

        WorkflowExecutionEntity executionRecord = workflowExecutionService.startExecution(request, context);
        UUID executionId = executionRecord.getExecutionId();
        UUID workflowId = executionRecord.getWorkflowId();
        String correlationId = context.getMetadata().getCorrelationId() != null
                ? context.getMetadata().getCorrelationId().toString()
                : null;

        int successSteps = 0;
        int failedSteps = 0;
        int skippedSteps = 0;

        for (com.aiqaos.core.engine.WorkflowStep<WorkflowRequest, WorkflowResponse> step : pipelineSteps) {
            context.setCurrentStep(step.getName());
            workflowExecutionService.updateCurrentStep(executionId, step.getName());

            int[] retryCounter = new int[1];
            long stepStart = System.currentTimeMillis();
            timelineService.recordEvent(executionId, workflowId, correlationId, "STEP_STARTED",
                    step.getName(), null, "RUNNING", null);

            WorkflowResponse stepResponse = executeWithRetry(step, request, context, retryCounter);
            long stepDuration = System.currentTimeMillis() - stepStart;
            boolean stepFailed = "FAILED".equals(stepResponse.getStatus());

            agentMetricsService.recordStepMetric(executionId, workflowId, step.getName(), "PIPELINE_STEP",
                    correlationId, stepDuration, null, null, !stepFailed,
                    stepFailed ? stepResponse.getMessage() : null);
            timelineService.recordEvent(executionId, workflowId, correlationId,
                    stepFailed ? "STEP_FAILED" : "STEP_COMPLETED", step.getName(), stepResponse.getMessage(),
                    stepResponse.getStatus(), stepDuration);

            if (stepFailed) {
                failedSteps++;
                // If ExecutionStep fails, we don't abort, we let BugAnalysisStep run (it will diagnose)
                if ("ExecutionStep".equals(step.getName())) {
                    continue;
                }

                context.setStatus(WorkflowStatus.FAILED);
                finalResponse.setStatus("FAILED");
                finalResponse.setMessage("Step " + step.getName() + " failed: " + stepResponse.getMessage());
                finalResponse.getOutputs().put("executionId", executionId);
                workflowExecutionService.completeExecution(executionId, finalResponse, context,
                        pipelineSteps.size(), successSteps, failedSteps, skippedSteps, retryCounter[0]);
                return finalResponse;
            }

            successSteps++;

            if ("BugAnalysisStep".equals(step.getName()) && context.getQaWorkflowState() != null) {
                bugAnalyticsService.recordBug(executionId, workflowId, context.getQaWorkflowState().getBugAnalysisReport());
            }
        }

        context.setStatus(WorkflowStatus.COMPLETED);
        finalResponse.getOutputs().put("executionId", executionId);
        workflowExecutionService.completeExecution(executionId, finalResponse, context,
                pipelineSteps.size(), successSteps, failedSteps, skippedSteps, 0);
        return finalResponse;
    }

    private WorkflowResponse executeWithRetry(
            com.aiqaos.core.engine.WorkflowStep<WorkflowRequest, WorkflowResponse> step,
            WorkflowRequest request,
            WorkflowContext context,
            int[] retryCounter) {

        int maxRetries = context.getRetryCount() > 0 ? context.getRetryCount() : 0;
        int attempt = 0;
        WorkflowResponse response = null;

        while (attempt <= maxRetries) {
            response = step.execute(request, context);
            if (!"FAILED".equals(response.getStatus())) {
                retryCounter[0] = attempt;
                return response;
            }
            attempt++;
            if (attempt <= maxRetries) {
                try {
                    // Backoff retry sleep
                    Thread.sleep(100 * attempt);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        retryCounter[0] = attempt;
        return response;
    }

    public List<com.aiqaos.core.engine.WorkflowStep<WorkflowRequest, WorkflowResponse>> getPipelineSteps() {
        return pipelineSteps;
    }
}
