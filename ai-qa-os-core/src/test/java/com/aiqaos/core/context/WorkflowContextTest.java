package com.aiqaos.core.context;

import com.aiqaos.core.enums.WorkflowStatus;
import com.aiqaos.core.requirement.RequirementContext;
import com.aiqaos.core.model.QAAnalysisResult;
import com.aiqaos.core.model.GeneratedTestCaseSuite;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.LearningFeedback;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class WorkflowContextTest {

    @Test
    public void testWorkflowContextStateAndStatus() {
        WorkflowContext context = new WorkflowContext();
        
        // Test basic workflow parameters
        context.setCurrentStep("RequirementReading");
        context.setPreviousStep("Start");
        context.setNextStep("Parser");
        context.setState("INITIALIZED");
        context.setRetryCount(2);

        assertEquals("RequirementReading", context.getCurrentStep());
        assertEquals("Start", context.getPreviousStep());
        assertEquals("Parser", context.getNextStep());
        assertEquals("INITIALIZED", context.getState());
        assertEquals(2, context.getRetryCount());

        // Test WorkflowStatus tracking
        context.setStatus(WorkflowStatus.RUNNING);
        assertEquals(WorkflowStatus.RUNNING, context.getStatus());

        context.setStatus(WorkflowStatus.COMPLETED);
        assertEquals(WorkflowStatus.COMPLETED, context.getStatus());
    }

    @Test
    public void testAutonomousQAWorkflowStateIntegration() {
        WorkflowContext context = new WorkflowContext();
        AutonomousQAWorkflowState state = new AutonomousQAWorkflowState();

        // 1. RequirementContext
        RequirementContext reqContext = new RequirementContext("Raw md story");
        reqContext.setTitle("Login feature");
        reqContext.setDescription("As a user...");
        state.setRequirementContext(reqContext);

        // 2. QAAnalysisResult
        QAAnalysisResult analysis = new QAAnalysisResult();
        analysis.setWorkflowId("wf-123");
        analysis.setAnalysisSummary("Login verification analysis");
        analysis.setIdentifiedScenarios(List.of("Successful Login", "Failed Login"));
        analysis.setReadyForTestDesign(true);
        state.setQaAnalysisResult(analysis);

        // 3. GeneratedTestCaseSuite
        GeneratedTestCaseSuite tcSuite = new GeneratedTestCaseSuite();
        tcSuite.setSuiteId("suite-456");
        GeneratedTestCaseSuite.TestCase tc = new GeneratedTestCaseSuite.TestCase();
        tc.setId("TC-001");
        tc.setName("Verify successful login");
        tc.setDescription("Ensure admin can login");
        tc.setSteps(List.of("Navigate to login", "Enter credentials", "Click login"));
        tc.setExpectedResult("Dashboard shown");
        tc.setPriority("HIGH");
        tcSuite.setTestCases(List.of(tc));
        state.setGeneratedTestCaseSuite(tcSuite);

        // 4. GeneratedScriptSuite
        GeneratedScriptSuite scriptSuite = new GeneratedScriptSuite();
        scriptSuite.setSuiteId("script-789");
        GeneratedScriptSuite.AutomationScript script = new GeneratedScriptSuite.AutomationScript();
        script.setScriptId("AS-001");
        script.setTestCaseId("TC-001");
        script.setTargetPlatform("WEB");
        script.setCode("await page.goto('/login');");
        script.setLanguage("JAVASCRIPT");
        scriptSuite.setScripts(List.of(script));
        state.setGeneratedScriptSuite(scriptSuite);

        // 5. ExecutionResult
        ExecutionResult execResult = new ExecutionResult();
        execResult.setTaskId("task-001");
        execResult.setAgentId("agent-001");
        execResult.setSuccess(true);
        execResult.setOutputData("Console logs...");
        state.setExecutionResult(execResult);

        // 6. BugAnalysisReport
        BugAnalysisReport bugReport = new BugAnalysisReport();
        bugReport.setReportId("bug-001");
        bugReport.setFailureReason("Timeout loading dashboard");
        bugReport.setStackTrace("TimeoutException...");
        bugReport.setSuspectedRootCauses(List.of("Slow network response"));
        bugReport.setRecommendation("Increase timeout to 10s");
        bugReport.setSelfHealable(true);
        state.setBugAnalysisReport(bugReport);

        // 7. LearningFeedback
        LearningFeedback feedback = new LearningFeedback();
        feedback.setFeedbackId("feedback-001");
        feedback.setWorkflowId("wf-123");
        feedback.setOptimizationSuggestions(List.of("Add more wait state check"));
        feedback.setSuccessfullySaved(true);
        state.setLearningFeedback(feedback);

        // Set state in context
        context.setQaWorkflowState(state);

        // Assertions
        assertNotNull(context.getQaWorkflowState());
        assertEquals("Login feature", context.getQaWorkflowState().getRequirementContext().getTitle());
        assertEquals("wf-123", context.getQaWorkflowState().getQaAnalysisResult().getWorkflowId());
        assertEquals("suite-456", context.getQaWorkflowState().getGeneratedTestCaseSuite().getSuiteId());
        assertEquals("TC-001", context.getQaWorkflowState().getGeneratedTestCaseSuite().getTestCases().get(0).getId());
        assertEquals("script-789", context.getQaWorkflowState().getGeneratedScriptSuite().getSuiteId());
        assertEquals("AS-001", context.getQaWorkflowState().getGeneratedScriptSuite().getScripts().get(0).getScriptId());
        assertEquals("task-001", context.getQaWorkflowState().getExecutionResult().getTaskId());
        assertTrue(context.getQaWorkflowState().getExecutionResult().isSuccess());
        assertEquals("bug-001", context.getQaWorkflowState().getBugAnalysisReport().getReportId());
        assertTrue(context.getQaWorkflowState().getBugAnalysisReport().isSelfHealable());
        assertEquals("feedback-001", context.getQaWorkflowState().getLearningFeedback().getFeedbackId());
        assertTrue(context.getQaWorkflowState().getLearningFeedback().isSuccessfullySaved());
    }
}
