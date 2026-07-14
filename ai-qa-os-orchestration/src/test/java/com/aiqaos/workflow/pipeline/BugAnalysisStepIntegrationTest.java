package com.aiqaos.workflow.pipeline;

import com.aiqaos.agent.impl.BugAnalyzerAgent;
import com.aiqaos.agent.registry.AgentRegistry;
import com.aiqaos.agent.manager.AgentManagerImpl;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedTestCaseSuite;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.contract.PromptRequest;
import com.aiqaos.core.contract.PromptResponse;
import com.aiqaos.core.engine.PromptEngine;
import com.aiqaos.provider.manager.LLMProviderManager;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import com.aiqaos.workflow.validation.LLMResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BugAnalysisStepIntegrationTest {

    private BugAnalysisStep step;
    private BugAnalyzerAgent agent;
    private AgentRegistry registry;
    private AgentManagerImpl manager;
    private LLMResponseValidator responseValidator;

    @BeforeEach
    public void setUp() {
        PromptEngine<PromptRequest, PromptResponse> promptEngine = new StubPromptEngine();
        String jsonResponse = "{\n" +
                "  \"reportId\": \"bug-1234\",\n" +
                "  \"rootCause\": \"Element not found for selector #submit\",\n" +
                "  \"failureCategory\": \"ELEMENT_NOT_FOUND\",\n" +
                "  \"impactedComponent\": \"Login Form\",\n" +
                "  \"confidence\": 0.9,\n" +
                "  \"selfHealingSuggestion\": \"Change selector to submit-btn\",\n" +
                "  \"requiresRegeneration\": true\n" +
                "}";
        LLMProviderManager providerManager = new StubLLMProviderManager(jsonResponse);

        agent = new BugAnalyzerAgent();
        ReflectionTestUtils.setField(agent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(agent, "providerManager", providerManager);

        registry = new AgentRegistry();
        registry.register(java.util.UUID.randomUUID(), agent);

        manager = new AgentManagerImpl();
        ReflectionTestUtils.setField(manager, "agentRegistry", registry);

        responseValidator = new LLMResponseValidator();
        ReflectionTestUtils.setField(responseValidator, "objectMapper", new ObjectMapper());

        step = new BugAnalysisStep();
        ReflectionTestUtils.setField(step, "agentManager", manager);
        ReflectionTestUtils.setField(step, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(step, "responseValidator", responseValidator);
    }

    @Test
    public void testBugAnalysisStepBypassedOnSuccess() {
        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        ExecutionResult execResult = new ExecutionResult();
        execResult.setSuccess(true);
        execResult.setFailed(0);
        context.getQaWorkflowState().setExecutionResult(execResult);

        com.aiqaos.core.contract.WorkflowResponse response = step.execute(request, context);

        assertEquals("SUCCESS", response.getStatus());
        assertTrue(response.getMessage().contains("Skipped BugAnalysisStep"));

        BugAnalysisReport report = context.getQaWorkflowState().getBugAnalysisReport();
        assertNotNull(report);
        assertEquals("NONE", report.getFailureCategory());
        assertEquals(1.0, report.getConfidence());
        assertEquals("CLOSED", report.getStatus());
    }

    @Test
    public void testBugAnalysisStepExecutedOnFailure() {
        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        // Set failures
        ExecutionResult execResult = new ExecutionResult();
        execResult.setSuccess(false);
        execResult.setFailed(1);
        context.getQaWorkflowState().setExecutionResult(execResult);

        context.getQaWorkflowState().setGeneratedTestCaseSuite(new GeneratedTestCaseSuite());
        context.getQaWorkflowState().setGeneratedScriptSuite(new GeneratedScriptSuite());

        com.aiqaos.core.contract.WorkflowResponse response = step.execute(request, context);

        assertEquals("SUCCESS", response.getStatus());

        BugAnalysisReport report = context.getQaWorkflowState().getBugAnalysisReport();
        assertNotNull(report);
        assertEquals("bug-1234", report.getReportId());
        assertEquals("Element not found for selector #submit", report.getRootCause());
        assertEquals("ELEMENT_NOT_FOUND", report.getFailureCategory());
        assertEquals(0.9, report.getConfidence());
        assertEquals("Change selector to submit-btn", report.getSelfHealingSuggestion());
        assertTrue(report.isRequiresRegeneration());
    }

    private static class StubPromptEngine implements PromptEngine<PromptRequest, PromptResponse> {
        @Override
        public PromptResponse renderPrompt(PromptRequest request) {
            PromptResponse res = new PromptResponse();
            res.setRenderedContent("Rendered Prompt Content");
            res.setStatus("SUCCESS");
            return res;
        }
    }

    private static class StubLLMProviderManager extends LLMProviderManager {
        private final String textResponse;
        public StubLLMProviderManager(String textResponse) {
            super(null, null, null, null, null, null, null);
            this.textResponse = textResponse;
        }
        @Override
        public LLMResponse generate(LLMRequest request) {
            return new LLMResponse(
                textResponse,
                "test-model",
                new TokenUsage(100, 150),
                300L
            );
        }
    }
}
