package com.aiqaos.orchestration.pipeline;

import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.orchestration.entity.WorkflowExecutionEntity;
import com.aiqaos.orchestration.review.HumanReviewService;
import com.aiqaos.orchestration.review.PausedWorkflowRegistry;
import com.aiqaos.orchestration.service.AgentMetricsService;
import com.aiqaos.orchestration.service.BugAnalyticsService;
import com.aiqaos.orchestration.service.TimelineService;
import com.aiqaos.orchestration.service.WorkflowExecutionService;
import com.aiqaos.testdata.synthetic.SyntheticGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WorkflowModeTest {

    @Mock private RequirementReaderStep requirementReaderStep;
    @Mock private QAAnalysisStep qaAnalysisStep;
    @Mock private TestCaseGenerationStep testCaseGenerationStep;
    @Mock private ScriptGenerationStep scriptGenerationStep;
    @Mock private ExecutionStep executionStep;
    @Mock private BugAnalysisStep bugAnalysisStep;
    @Mock private ReportingStep reportingStep;
    @Mock private LearningStep learningStep;
    @Mock private SelfHealingStep selfHealingStep;

    @Mock private WorkflowExecutionService workflowExecutionService;
    @Mock private TimelineService timelineService;
    @Mock private AgentMetricsService agentMetricsService;
    @Mock private BugAnalyticsService bugAnalyticsService;
    @Mock private PausedWorkflowRegistry pausedWorkflowRegistry;
    @Mock private HumanReviewService humanReviewService;

    private AutonomousQAPipelineOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(requirementReaderStep.getName()).thenReturn("RequirementReaderStep");
        when(qaAnalysisStep.getName()).thenReturn("QAAnalysisStep");
        when(testCaseGenerationStep.getName()).thenReturn("TestCaseGenerationStep");
        when(scriptGenerationStep.getName()).thenReturn("ScriptGenerationStep");
        when(executionStep.getName()).thenReturn("ExecutionStep");
        when(bugAnalysisStep.getName()).thenReturn("BugAnalysisStep");
        when(reportingStep.getName()).thenReturn("ReportingStep");
        when(learningStep.getName()).thenReturn("LearningStep");
        when(selfHealingStep.getName()).thenReturn("SelfHealingStep");

        WorkflowResponse okResponse = new WorkflowResponse();
        okResponse.setStatus("SUCCESS");
        when(requirementReaderStep.execute(any(), any())).thenReturn(okResponse);
        when(qaAnalysisStep.execute(any(), any())).thenReturn(okResponse);
        when(testCaseGenerationStep.execute(any(), any())).thenReturn(okResponse);
        when(scriptGenerationStep.execute(any(), any())).thenReturn(okResponse);
        when(executionStep.execute(any(), any())).thenReturn(okResponse);
        when(bugAnalysisStep.execute(any(), any())).thenReturn(okResponse);
        when(reportingStep.execute(any(), any())).thenReturn(okResponse);
        when(learningStep.execute(any(), any())).thenReturn(okResponse);
        when(selfHealingStep.execute(any(), any())).thenReturn(okResponse);

        WorkflowExecutionEntity entity = new WorkflowExecutionEntity();
        entity.setId(UUID.randomUUID());
        entity.setWorkflowId(UUID.randomUUID());
        when(workflowExecutionService.startExecution(any(), any())).thenReturn(entity);

        orchestrator = new AutonomousQAPipelineOrchestrator(
                requirementReaderStep, qaAnalysisStep, testCaseGenerationStep,
                scriptGenerationStep, executionStep, bugAnalysisStep,
                reportingStep, learningStep, selfHealingStep,
                workflowExecutionService, timelineService, agentMetricsService,
                bugAnalyticsService, new TestingObjectProvider<>(null),
                pausedWorkflowRegistry, humanReviewService
        );
    }

    @Test
    @DisplayName("WF-1: SCENARIO_ONLY mode completes after test case generation and skips execution")
    void testScenarioOnlyMode() {
        WorkflowRequest req = new WorkflowRequest();
        req.setWorkflowName("AUTONOMOUS_QA_PIPELINE");
        req.getInputs().put("workflowMode", "SCENARIO_ONLY");

        WorkflowResponse response = orchestrator.runPipeline(req, new WorkflowContext());

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertTrue(response.getMessage().contains("SCENARIO_ONLY mode"));
        assertEquals("SCENARIO_ONLY", response.getOutputs().get("workflowMode"));

        verify(requirementReaderStep).execute(any(), any());
        verify(qaAnalysisStep).execute(any(), any());
        verify(testCaseGenerationStep).execute(any(), any());
        verify(scriptGenerationStep, never()).execute(any(), any());
        verify(executionStep, never()).execute(any(), any());
    }

    @Test
    @DisplayName("WF-1: TEST_DATA_SYNTHESIS mode generates synthetic fixtures and completes early")
    void testDataSynthesisMode() {
        WorkflowRequest req = new WorkflowRequest();
        req.setWorkflowName("AUTONOMOUS_QA_PIPELINE");
        req.getInputs().put("workflowMode", "TEST_DATA_SYNTHESIS");
        req.getInputs().put("datasetType", "PAYMENT_METHOD");

        WorkflowResponse response = orchestrator.runPipeline(req, new WorkflowContext());

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertTrue(response.getMessage().contains("TEST_DATA_SYNTHESIS mode"));
        assertEquals("TEST_DATA_SYNTHESIS", response.getOutputs().get("workflowMode"));

        verify(requirementReaderStep).execute(any(), any());
        verify(qaAnalysisStep).execute(any(), any());
        verify(testCaseGenerationStep).execute(any(), any());
        verify(executionStep, never()).execute(any(), any());
    }

    private static class TestingObjectProvider<T> implements org.springframework.beans.factory.ObjectProvider<T> {
        private final T instance;
        TestingObjectProvider(T instance) { this.instance = instance; }
        @Override public T getObject() { return instance; }
        @Override public T getObject(Object... args) { return instance; }
        @Override public T getIfAvailable() { return instance; }
        @Override public T getIfUnique() { return instance; }
    }
}
