package com.aiqaos.workflow.pipeline;

import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.enums.WorkflowStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class AutonomousQAPipelineOrchestrator {

    private final List<com.aiqaos.core.engine.WorkflowStep<WorkflowRequest, WorkflowResponse>> pipelineSteps = new ArrayList<>();

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
            SelfHealingStep selfHealingStep) {
        
        pipelineSteps.add(requirementReaderStep);
        pipelineSteps.add(qaAnalysisStep);
        pipelineSteps.add(testCaseGenerationStep);
        pipelineSteps.add(scriptGenerationStep);
        pipelineSteps.add(executionStep);
        pipelineSteps.add(bugAnalysisStep);
        pipelineSteps.add(reportingStep);
        pipelineSteps.add(learningStep);
        pipelineSteps.add(selfHealingStep);
    }

    public WorkflowResponse runPipeline(WorkflowRequest request, WorkflowContext context) {
        context.setStatus(WorkflowStatus.RUNNING);
        WorkflowResponse finalResponse = new WorkflowResponse();
        finalResponse.setStatus("SUCCESS");
        finalResponse.setMessage("Pipeline executed successfully");

        for (com.aiqaos.core.engine.WorkflowStep<WorkflowRequest, WorkflowResponse> step : pipelineSteps) {
            context.setCurrentStep(step.getName());

            WorkflowResponse stepResponse = executeWithRetry(step, request, context);
            if ("FAILED".equals(stepResponse.getStatus())) {
                // If ExecutionStep fails, we don't abort, we let BugAnalysisStep run (it will diagnose)
                if ("ExecutionStep".equals(step.getName())) {
                    continue;
                }
                
                context.setStatus(WorkflowStatus.FAILED);
                finalResponse.setStatus("FAILED");
                finalResponse.setMessage("Step " + step.getName() + " failed: " + stepResponse.getMessage());
                return finalResponse;
            }
        }

        context.setStatus(WorkflowStatus.COMPLETED);
        return finalResponse;
    }

    private WorkflowResponse executeWithRetry(
            com.aiqaos.core.engine.WorkflowStep<WorkflowRequest, WorkflowResponse> step,
            WorkflowRequest request,
            WorkflowContext context) {
        
        int maxRetries = context.getRetryCount() > 0 ? context.getRetryCount() : 0;
        int attempt = 0;
        WorkflowResponse response = null;

        while (attempt <= maxRetries) {
            response = step.execute(request, context);
            if (!"FAILED".equals(response.getStatus())) {
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
        return response;
    }

    public List<com.aiqaos.core.engine.WorkflowStep<WorkflowRequest, WorkflowResponse>> getPipelineSteps() {
        return pipelineSteps;
    }
}
