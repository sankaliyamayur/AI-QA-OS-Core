package com.aiqaos.orchestration.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.contract.ConfidenceGate;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.orchestration.entity.WorkflowExecutionEntity;
import com.aiqaos.orchestration.review.HumanReviewService;
import com.aiqaos.orchestration.review.PausedWorkflowRegistry;
import com.aiqaos.orchestration.service.AgentMetricsService;
import com.aiqaos.orchestration.service.BugAnalyticsService;
import com.aiqaos.orchestration.service.TimelineService;
import com.aiqaos.orchestration.service.WorkflowExecutionService;
import com.aiqaos.provider.exception.AllProvidersExhaustedException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * A provider outage must fail the run — even inside {@code ExecutionStep}, which is otherwise
 * allowed to fail and continue.
 *
 * <p><b>The hole this closes.</b> {@code ExecutionStep} is deliberately special-cased to
 * {@code continue} on failure so {@code BugAnalysisStep} can diagnose a genuinely failing test; that
 * is why a run with a failed execution still reports SUCCESS at 8/9. But {@code ExecutionStep} also
 * drives {@code EXECUTION_ENGINEER} through the LLM, so before this change an exhausted provider
 * chain inside that step was swallowed by the same {@code continue} and the pipeline reported
 * SUCCESS for a run in which the AI never answered.
 *
 * <p>A failing test is a result worth reporting. Having no AI to run the test with is not a result,
 * and must never be dressed up as a green run.
 *
 * <p>Detection is by message marker rather than exception type on purpose: every pipeline step
 * catches {@code Exception} broadly and collapses it into {@code setStatus("FAILED")} with
 * {@code e.getMessage()} appended, so the type is gone long before the orchestrator sees it.
 */
class ProviderExhaustionIsFatalTest {

    private static final String EXHAUSTION_MESSAGE =
            "Failed in ExecutionStep: " + AllProvidersExhaustedException.MARKER
                    + ": all 3 LLM provider(s) failed — openai: HTTP 429 rate limited; "
                    + "claude: HTTP 401 unauthorized; gemini: HTTP 500 server error";

    private final WorkflowExecutionService workflowExecutionService = mock(WorkflowExecutionService.class);
    private final TimelineService timelineService = mock(TimelineService.class);
    private final AgentMetricsService agentMetricsService = mock(AgentMetricsService.class);
    private final BugAnalyticsService bugAnalyticsService = mock(BugAnalyticsService.class);

    private WorkflowResponse ok(String message) {
        WorkflowResponse r = new WorkflowResponse();
        r.setStatus("SUCCESS");
        r.setMessage(message);
        return r;
    }

    private WorkflowResponse failed(String message) {
        WorkflowResponse r = new WorkflowResponse();
        r.setStatus("FAILED");
        r.setMessage(message);
        return r;
    }

    /** Builds an orchestrator whose ExecutionStep fails with the supplied message. */
    private Harness harnessWithFailingExecution(String executionFailureMessage) {
        RequirementReaderStep requirementReaderStep = mock(RequirementReaderStep.class);
        QAAnalysisStep qaAnalysisStep = mock(QAAnalysisStep.class);
        TestCaseGenerationStep testCaseGenerationStep = mock(TestCaseGenerationStep.class);
        ScriptGenerationStep scriptGenerationStep = mock(ScriptGenerationStep.class);
        ExecutionStep executionStep = mock(ExecutionStep.class);
        BugAnalysisStep bugAnalysisStep = mock(BugAnalysisStep.class);
        ReportingStep reportingStep = mock(ReportingStep.class);
        LearningStep learningStep = mock(LearningStep.class);
        SelfHealingStep selfHealingStep = mock(SelfHealingStep.class);

        stubName(requirementReaderStep, "RequirementReaderStep");
        stubName(qaAnalysisStep, "QAAnalysisStep");
        stubName(testCaseGenerationStep, "TestCaseGenerationStep");
        stubName(scriptGenerationStep, "ScriptGenerationStep");
        stubName(executionStep, "ExecutionStep");
        stubName(bugAnalysisStep, "BugAnalysisStep");
        stubName(reportingStep, "ReportingStep");
        stubName(learningStep, "LearningStep");
        stubName(selfHealingStep, "SelfHealingStep");

        when(requirementReaderStep.execute(any(), any())).thenReturn(ok("read"));
        when(qaAnalysisStep.execute(any(), any())).thenReturn(ok("analysed"));
        when(testCaseGenerationStep.execute(any(), any())).thenReturn(ok("cases"));
        when(scriptGenerationStep.execute(any(), any())).thenReturn(ok("scripts"));
        when(executionStep.execute(any(), any())).thenReturn(failed(executionFailureMessage));
        when(bugAnalysisStep.execute(any(), any())).thenReturn(ok("diagnosed"));
        when(reportingStep.execute(any(), any())).thenReturn(ok("reported"));
        when(learningStep.execute(any(), any())).thenReturn(ok("learned"));
        when(selfHealingStep.execute(any(), any())).thenReturn(ok("healed"));

        WorkflowExecutionEntity executionRecord = new WorkflowExecutionEntity();
        executionRecord.setExecutionId(UUID.randomUUID());
        executionRecord.setWorkflowId(UUID.randomUUID());
        when(workflowExecutionService.startExecution(any(), any())).thenReturn(executionRecord);

        ObjectProvider<ConfidenceGate> noGate = new ObjectProvider<>() {
            @Override public ConfidenceGate getObject(Object... args) { return null; }
            @Override public ConfidenceGate getObject() { return null; }
            @Override public ConfidenceGate getIfAvailable() { return null; }
            @Override public ConfidenceGate getIfUnique() { return null; }
        };

        AutonomousQAPipelineOrchestrator orchestrator = new AutonomousQAPipelineOrchestrator(
                requirementReaderStep, qaAnalysisStep, testCaseGenerationStep, scriptGenerationStep,
                executionStep, bugAnalysisStep, reportingStep, learningStep, selfHealingStep,
                workflowExecutionService, timelineService, agentMetricsService, bugAnalyticsService,
                noGate, new PausedWorkflowRegistry(), mock(HumanReviewService.class));

        return new Harness(orchestrator, bugAnalysisStep, reportingStep);
    }

    private static void stubName(Object step, String name) {
        when(((com.aiqaos.core.engine.WorkflowStep<?, ?>) step).getName()).thenReturn(name);
    }

    private record Harness(AutonomousQAPipelineOrchestrator orchestrator,
                           BugAnalysisStep bugAnalysisStep,
                           ReportingStep reportingStep) {
    }

    private static WorkflowRequest request() {
        WorkflowRequest r = new WorkflowRequest();
        r.setWorkflowName("provider-exhaustion-regression");
        return r;
    }

    private static WorkflowContext context() {
        WorkflowContext c = new WorkflowContext();
        c.setQaWorkflowState(new AutonomousQAWorkflowState());
        return c;
    }

    // ── the mandatory requirement ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("provider exhaustion inside ExecutionStep must NOT report SUCCESS")
    void providerExhaustionInExecutionStepFailsTheWorkflow() {
        Harness h = harnessWithFailingExecution(EXHAUSTION_MESSAGE);

        WorkflowResponse response = h.orchestrator().runPipeline(request(), context());

        assertEquals("FAILED", response.getStatus(),
                "a run whose AI provider chain was exhausted is not a green run");
        assertNotEquals("SUCCESS", response.getStatus());
        assertTrue(response.getMessage().contains("no AI provider could serve the request"),
                "the failure must say why, not just that a step failed: " + response.getMessage());
        assertEquals(Boolean.TRUE, response.getOutputs().get("providerExhausted"));
    }

    @Test
    @DisplayName("exhaustion aborts the pipeline instead of continuing to later steps")
    void exhaustionStopsTheRestOfThePipeline() {
        Harness h = harnessWithFailingExecution(EXHAUSTION_MESSAGE);

        h.orchestrator().runPipeline(request(), context());

        // Without the guard these would run and the pipeline would finish green.
        verify(h.bugAnalysisStep(), never()).execute(any(), any());
        verify(h.reportingStep(), never()).execute(any(), any());
    }

    // ── the behaviour that must NOT regress ───────────────────────────────────────────────────────

    @Test
    @DisplayName("an ordinary ExecutionStep failure still continues to BugAnalysis and reports SUCCESS")
    void anOrdinaryTestFailureStillContinues() {
        Harness h = harnessWithFailingExecution("Failed in ExecutionStep: 2 of 3 assertions failed");

        WorkflowResponse response = h.orchestrator().runPipeline(request(), context());

        assertEquals("SUCCESS", response.getStatus(),
                "a failing test under test is a legitimate result — this special case must survive");
        verify(h.bugAnalysisStep()).execute(any(), any());
    }

    @Test
    @DisplayName("the marker is matched anywhere in the message, however the step wrapped it")
    void markerIsDetectedThroughStepWrapping() {
        assertTrue(AllProvidersExhaustedException.isExhaustion(EXHAUSTION_MESSAGE));
        assertTrue(AllProvidersExhaustedException.isExhaustion(
                "Failed in QAAnalysisStep: wrapped: " + AllProvidersExhaustedException.MARKER + ": ..."));
        assertTrue(!AllProvidersExhaustedException.isExhaustion("Failed in ExecutionStep: element not found"));
        assertTrue(!AllProvidersExhaustedException.isExhaustion(null));
    }
}
