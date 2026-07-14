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
import com.aiqaos.workflow.validation.LLMResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

public class BugAnalyzerAgentFailureRecoveryTest {

    private BugAnalysisStep step;
    private BugAnalyzerAgent agent;
    private AgentRegistry registry;
    private AgentManagerImpl manager;
    private LLMResponseValidator responseValidator;

    @BeforeEach
    public void setUp() {
        PromptEngine<PromptRequest, PromptResponse> promptEngine = new StubPromptEngine();
        
        // Stub provider to throw an exception representing a model error / timeout
        LLMProviderManager providerManager = new FaultyLLMProviderManager();

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
    public void testBugAnalyzerAgentFailureRecovery() {
        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        ExecutionResult execResult = new ExecutionResult();
        execResult.setSuccess(false);
        execResult.setFailed(1);
        execResult.setErrorMessage("Element not found: '#submit'");
        context.getQaWorkflowState().setExecutionResult(execResult);

        context.getQaWorkflowState().setGeneratedTestCaseSuite(new GeneratedTestCaseSuite());
        context.getQaWorkflowState().setGeneratedScriptSuite(new GeneratedScriptSuite());

        // Execute Step - should NOT fail or throw exception
        com.aiqaos.core.contract.WorkflowResponse response = step.execute(request, context);

        assertEquals("SUCCESS", response.getStatus());

        BugAnalysisReport report = context.getQaWorkflowState().getBugAnalysisReport();
        assertNotNull(report);
        assertTrue(report.getReportId().startsWith("bug-report-fallback-"));
        assertEquals("AGENT_FAILURE", report.getFailureCategory());
        assertEquals(0.0, report.getConfidence());
        assertFalse(report.isRequiresRegeneration());
        assertNotNull(report.getCreatedTime());
        assertEquals("HIGH", report.getSeverity());
        assertEquals("P1", report.getPriority());
        assertEquals("OPEN", report.getStatus());
        assertTrue(report.getRootCause().contains("Agent execution failed:"));
        assertTrue(report.getRootCause().contains("Simulated LLM timeout/error"));
    }

    private static class StubPromptEngine implements PromptEngine<PromptRequest, PromptResponse> {
        @Override
        public PromptResponse renderPrompt(PromptRequest request) {
            PromptResponse res = new PromptResponse();
            res.setRenderedContent("Rendered Strategy Prompt");
            res.setStatus("SUCCESS");
            return res;
        }
    }

    private static class FaultyLLMProviderManager extends LLMProviderManager {
        public FaultyLLMProviderManager() {
            super(null, null, null, null, null, null, null);
        }
        @Override
        public LLMResponse generate(LLMRequest request) {
            throw new RuntimeException("Simulated LLM timeout/error");
        }
    }
}
