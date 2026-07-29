package com.aiqaos.orchestration.pipeline;

import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.enums.WorkflowStatus;
import com.aiqaos.core.contract.ConfidenceGate;
import com.aiqaos.core.contract.ConfidenceVerdict;
import com.aiqaos.core.contract.ConfidenceDecisionContext;
import org.springframework.beans.factory.ObjectProvider;
import com.aiqaos.orchestration.entity.WorkflowExecutionEntity;
import com.aiqaos.orchestration.review.HumanReviewService;
import com.aiqaos.orchestration.review.PausedRun;
import com.aiqaos.orchestration.review.PausedWorkflowRegistry;
import com.aiqaos.orchestration.service.AgentMetricsService;
import com.aiqaos.orchestration.service.BugAnalyticsService;
import com.aiqaos.orchestration.service.TimelineService;
import com.aiqaos.orchestration.service.WorkflowExecutionService;
import com.aiqaos.observability.trace.TraceManager;
import com.aiqaos.observability.trace.CorrelationTraceBridge;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class AutonomousQAPipelineOrchestrator {

    private final List<com.aiqaos.core.engine.WorkflowStep<WorkflowRequest, WorkflowResponse>> pipelineSteps = new ArrayList<>();

    private final WorkflowExecutionService workflowExecutionService;
    private final TimelineService timelineService;
    private final AgentMetricsService agentMetricsService;
    private final BugAnalyticsService bugAnalyticsService;
    // AI-1: optional — the Brain provides the impl at runtime; absent in orchestration's own tests.
    private final ObjectProvider<ConfidenceGate> confidenceGateProvider;
    // AI-2: human-in-the-loop approval.
    private final PausedWorkflowRegistry pausedWorkflowRegistry;
    private final HumanReviewService humanReviewService;

    // OBS-1: optional tracing (field-injected so direct construction in tests leaves them null → no spans).
    @Autowired(required = false)
    private TraceManager traceManager;
    @Autowired(required = false)
    private CorrelationTraceBridge correlationTraceBridge;

    // WF-3: Learning analysis components (field-injected for non-breaking spring autowiring)
    @Autowired(required = false)
    private com.aiqaos.learning.analysis.FlakyTestDetector flakyTestDetector;
    @Autowired(required = false)
    private com.aiqaos.learning.analysis.FailedTestRerunSelector failedTestRerunSelector;
    @Autowired(required = false)
    private com.aiqaos.learning.analysis.TestImpactAnalyzer testImpactAnalyzer;

    // WF-1: Synthetic data generator for TEST_DATA_SYNTHESIS workflow mode
    @Autowired(required = false)
    private com.aiqaos.testdata.synthetic.SyntheticGenerator syntheticGenerator;

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
            BugAnalyticsService bugAnalyticsService,
            ObjectProvider<ConfidenceGate> confidenceGateProvider,
            PausedWorkflowRegistry pausedWorkflowRegistry,
            HumanReviewService humanReviewService) {

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
        this.confidenceGateProvider = confidenceGateProvider;
        this.pausedWorkflowRegistry = pausedWorkflowRegistry;
        this.humanReviewService = humanReviewService;
    }

    public WorkflowResponse runPipeline(WorkflowRequest request, WorkflowContext context) {
        context.setStatus(WorkflowStatus.RUNNING);
        WorkflowExecutionEntity executionRecord = workflowExecutionService.startExecution(request, context);
        UUID executionId = executionRecord.getExecutionId();
        UUID workflowId = executionRecord.getWorkflowId();
        String correlationId = context.getMetadata().getCorrelationId() != null
                ? context.getMetadata().getCorrelationId().toString()
                : null;

        // OBS-1: a run span for the whole workflow (no-op when tracing beans are absent, e.g. tests).
        Span runSpan = traceManager != null ? traceManager.startSpan("workflow.run") : null;
        Scope scope = runSpan != null ? runSpan.makeCurrent() : null;
        try {
            // MNT-6 + OBS-1: correlationId (and traceId) into MDC and onto the span, so logs correlate
            // with the trace. Falls back to a bare MDC put when the bridge bean is absent.
            if (correlationTraceBridge != null) {
                correlationTraceBridge.bindCorrelationId(correlationId);
                correlationTraceBridge.bindTraceId();
            } else if (correlationId != null) {
                org.slf4j.MDC.put("correlationId", correlationId);
            }
            if (runSpan != null && workflowId != null) {
                runSpan.setAttribute("workflow.id", workflowId.toString());
            }
            return executeFrom(request, context, 0, executionId, workflowId, correlationId, new int[]{0, 0, 0});
        } finally {
            if (scope != null) {
                scope.close();
            }
            if (runSpan != null) {
                traceManager.endSpan(runSpan);
            }
            if (correlationTraceBridge != null) {
                correlationTraceBridge.clear();
            } else {
                org.slf4j.MDC.remove("correlationId");
            }
        }
    }

    /**
     * AI-2: resume a run paused for human review (approved), continuing from the paused step with
     * its retained context. Returns NOT_FOUND if no paused run is registered for the workflow.
     */
    public WorkflowResponse resume(UUID workflowId, String reviewer, String comment) {
        PausedRun run = pausedWorkflowRegistry.remove(workflowId);
        if (run == null) {
            WorkflowResponse r = new WorkflowResponse();
            r.setStatus("NOT_FOUND");
            r.setMessage("No paused run awaiting review for workflow " + workflowId);
            return r;
        }
        humanReviewService.markApproved(workflowId, reviewer, comment);
        run.getContext().setStatus(WorkflowStatus.RUNNING);
        timelineService.recordEvent(run.getExecutionId(), workflowId, run.getCorrelationId(),
                "HUMAN_APPROVED", run.getStepName(), "Approved by " + reviewer, "RUNNING", null);
        return executeFrom(run.getRequest(), run.getContext(), run.getNextStepIndex(),
                run.getExecutionId(), workflowId, run.getCorrelationId(), run.getCounters());
    }

    /**
     * AI-2: reject a run paused for human review — cancels the workflow and finalizes the record.
     */
    public WorkflowResponse reject(UUID workflowId, String reviewer, String comment) {
        PausedRun run = pausedWorkflowRegistry.remove(workflowId);
        WorkflowResponse r = new WorkflowResponse();
        if (run == null) {
            r.setStatus("NOT_FOUND");
            r.setMessage("No paused run awaiting review for workflow " + workflowId);
            return r;
        }
        humanReviewService.markRejected(workflowId, reviewer, comment);
        run.getContext().setStatus(WorkflowStatus.CANCELLED);
        r.setStatus("CANCELLED");
        r.setRunState("REJECTED");
        r.setMessage("Rejected by " + reviewer + (comment != null ? ": " + comment : ""));
        r.getOutputs().put("executionId", run.getExecutionId());
        timelineService.recordEvent(run.getExecutionId(), workflowId, run.getCorrelationId(),
                "HUMAN_REJECTED", run.getStepName(), r.getMessage(), "CANCELLED", null);
        workflowExecutionService.completeExecution(run.getExecutionId(), r, run.getContext(),
                pipelineSteps.size(), run.getCounters()[0], run.getCounters()[1], run.getCounters()[2], 0);
        return r;
    }

    /**
     * Runs the pipeline from {@code startIndex}. Used both for a fresh run (index 0) and to resume a
     * HUMAN_REVIEW pause. {@code counters} = {success, failed, skipped}, carried across a pause/resume.
     */
    private WorkflowResponse executeFrom(WorkflowRequest request, WorkflowContext context, int startIndex,
                                         UUID executionId, UUID workflowId, String correlationId, int[] counters) {
        WorkflowResponse finalResponse = new WorkflowResponse();
        finalResponse.setStatus("SUCCESS");
        finalResponse.setMessage("Pipeline executed successfully");

        for (int i = startIndex; i < pipelineSteps.size(); i++) {
            com.aiqaos.core.engine.WorkflowStep<WorkflowRequest, WorkflowResponse> step = pipelineSteps.get(i);
            context.setCurrentStep(step.getName());
            workflowExecutionService.updateCurrentStep(executionId, step.getName());

            int[] retryCounter = new int[1];
            long stepStart = System.currentTimeMillis();

            // OBS-1: a child span per step (no-op when tracing is absent), nesting under the run span.
            Span stepSpan = traceManager != null ? traceManager.startSpan("workflow.step." + step.getName()) : null;
            if (stepSpan != null) {
                stepSpan.setAttribute("step.name", step.getName());
            }
            Scope stepScope = stepSpan != null ? stepSpan.makeCurrent() : null;

            timelineService.recordEvent(executionId, workflowId, correlationId, "STEP_STARTED",
                    step.getName(), null, "RUNNING", null);

            WorkflowResponse stepResponse;
            try {
                stepResponse = executeWithRetry(step, request, context, retryCounter);
            } finally {
                if (stepScope != null) {
                    stepScope.close();
                }
                if (stepSpan != null) {
                    traceManager.endSpan(stepSpan);
                }
            }
            long stepDuration = System.currentTimeMillis() - stepStart;
            boolean stepFailed = "FAILED".equals(stepResponse.getStatus());

            agentMetricsService.recordStepMetric(executionId, workflowId, step.getName(), "PIPELINE_STEP",
                    correlationId, stepDuration, null, null, !stepFailed,
                    stepFailed ? stepResponse.getMessage() : null);
            timelineService.recordEvent(executionId, workflowId, correlationId,
                    stepFailed ? "STEP_FAILED" : "STEP_COMPLETED", step.getName(), stepResponse.getMessage(),
                    stepResponse.getStatus(), stepDuration);

            if (stepFailed) {
                counters[1]++;
                // If ExecutionStep fails, we don't abort, we let BugAnalysisStep run (it will diagnose)
                if ("ExecutionStep".equals(step.getName())) {
                    continue;
                }

                context.setStatus(WorkflowStatus.FAILED);
                finalResponse.setStatus("FAILED");
                finalResponse.setMessage("Step " + step.getName() + " failed: " + stepResponse.getMessage());
                finalResponse.getOutputs().put("executionId", executionId);
                workflowExecutionService.completeExecution(executionId, finalResponse, context,
                        pipelineSteps.size(), counters[0], counters[1], counters[2], retryCounter[0]);
                return finalResponse;
            }

            counters[0]++;

            // WF-1: Check distinct workflow execution modes (SCENARIO_ONLY | TEST_DATA_SYNTHESIS | FULL_E2E)
            Object modeObj = request.getInputs() != null ? request.getInputs().get("workflowMode") : null;
            String workflowMode = modeObj != null ? modeObj.toString().toUpperCase() : "FULL_E2E";

            if ("TestCaseGenerationStep".equals(step.getName())) {
                if ("SCENARIO_ONLY".equals(workflowMode)) {
                    finalResponse.setMessage("Scenario generation workflow completed successfully (SCENARIO_ONLY mode)");
                    finalResponse.getOutputs().put("workflowMode", "SCENARIO_ONLY");
                    finalResponse.getOutputs().put("executionId", executionId);
                    counters[2] += (pipelineSteps.size() - i - 1); // record skipped steps
                    context.setStatus(WorkflowStatus.COMPLETED);
                    workflowExecutionService.completeExecution(executionId, finalResponse, context,
                            pipelineSteps.size(), counters[0], counters[1], counters[2], retryCounter[0]);
                    return finalResponse;
                } else if ("TEST_DATA_SYNTHESIS".equals(workflowMode)) {
                    if (syntheticGenerator != null) {
                        String datasetType = (String) request.getInputs().getOrDefault("datasetType", "USER_PROFILE");
                        Map<String, Object> fixture = syntheticGenerator.generateFixture(datasetType);
                        finalResponse.getOutputs().put("testDataFixture", fixture);
                        context.getVariables().put("testDataFixture", fixture);
                    }
                    finalResponse.setMessage("Test data synthesis workflow completed successfully (TEST_DATA_SYNTHESIS mode)");
                    finalResponse.getOutputs().put("workflowMode", "TEST_DATA_SYNTHESIS");
                    finalResponse.getOutputs().put("executionId", executionId);
                    counters[2] += (pipelineSteps.size() - i - 1);
                    context.setStatus(WorkflowStatus.COMPLETED);
                    workflowExecutionService.completeExecution(executionId, finalResponse, context,
                            pipelineSteps.size(), counters[0], counters[1], counters[2], retryCounter[0]);
                    return finalResponse;
                }
            }

            if ("BugAnalysisStep".equals(step.getName()) && bugAnalyticsService != null && context.getQaWorkflowState() != null && context.getQaWorkflowState().getBugAnalysisReport() != null) {
                bugAnalyticsService.recordBug(executionId, workflowId, context.getQaWorkflowState().getBugAnalysisReport());
            }

            // AI-1: confidence gate — evaluate the step's reported confidence and route.
            // Owned by the Brain (core interface, brain impl); absent → permissive (no-op).
            ConfidenceGate gate = confidenceGateProvider.getIfAvailable();
            if (gate != null) {
                ConfidenceVerdict verdict = gate.evaluate(
                        new ConfidenceDecisionContext(step.getName(), stepResponse.getConfidence(), correlationId));
                if (verdict == ConfidenceVerdict.HUMAN_REVIEW) {
                    // AI-2: retain the run for resume (do NOT finalize) and record a PENDING review.
                    context.setStatus(WorkflowStatus.PAUSED);
                    pausedWorkflowRegistry.register(new PausedRun(workflowId, executionId, correlationId,
                            request, context, i + 1, step.getName(), stepResponse.getConfidence(), counters));
                    humanReviewService.createPending(workflowId, executionId, step.getName(), stepResponse.getConfidence());
                    workflowExecutionService.updateStatus(executionId, "PAUSED", step.getName());
                    timelineService.recordEvent(executionId, workflowId, correlationId, "HUMAN_REVIEW",
                            step.getName(), "Paused for human review (confidence " + stepResponse.getConfidence() + ")",
                            "PAUSED", null);
                    finalResponse.setStatus("PAUSED");
                    finalResponse.setRunState("HUMAN_REVIEW");
                    finalResponse.setMessage("Paused for human review after step " + step.getName());
                    finalResponse.getOutputs().put("executionId", executionId);
                    finalResponse.getOutputs().put("humanReviewStep", step.getName());
                    return finalResponse;
                }
                // PROCEED / PROCEED_WITH_VALIDATION / UNGATED → continue (verdict recorded by the gate).
            }
        }

        context.setStatus(WorkflowStatus.COMPLETED);
        finalResponse.getOutputs().put("executionId", executionId);
        workflowExecutionService.completeExecution(executionId, finalResponse, context,
                pipelineSteps.size(), counters[0], counters[1], counters[2], 0);
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

    // WF-3: Test Impact Analysis helper
    public List<String> selectImpactedSteps(List<String> modifiedFiles, List<String> availableStepNames) {
        if (testImpactAnalyzer != null) {
            return testImpactAnalyzer.analyzeImpactedSteps(modifiedFiles, availableStepNames);
        }
        return availableStepNames != null ? availableStepNames : java.util.Collections.emptyList();
    }

    // WF-3: Failed Test Re-run selector helper
    public List<String> selectFailedStepsForRerun(List<com.aiqaos.learning.analysis.FailedTestRerunSelector.StepExecutionRecord> stepRecords) {
        if (failedTestRerunSelector != null) {
            return failedTestRerunSelector.selectFailedStepsForRerun(stepRecords);
        }
        return java.util.Collections.emptyList();
    }

    // WF-3: Flaky Test analysis helper
    public com.aiqaos.learning.analysis.FlakyTestReport analyzeStepFlakiness(String stepName, List<String> executionOutcomes) {
        if (flakyTestDetector != null) {
            return flakyTestDetector.analyzeStepHistory(stepName, executionOutcomes);
        }
        return new com.aiqaos.learning.analysis.FlakyTestReport(stepName, 0, 0, 0, 0.0, com.aiqaos.learning.analysis.FlakyTestReport.Recommendation.STABLE);
    }
}
