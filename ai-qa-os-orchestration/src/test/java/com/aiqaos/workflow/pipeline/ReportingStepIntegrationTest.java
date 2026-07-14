package com.aiqaos.workflow.pipeline;

import com.aiqaos.agent.impl.ReportingAgent;
import com.aiqaos.agent.registry.AgentRegistry;
import com.aiqaos.agent.manager.AgentManagerImpl;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.QAExecutionReport;
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
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ReportingStepIntegrationTest {

    private ReportingStep step;
    private ReportingAgent agent;
    private AgentRegistry registry;
    private AgentManagerImpl manager;
    private LLMResponseValidator responseValidator;

    @BeforeEach
    public void setUp() {
        PromptEngine<PromptRequest, PromptResponse> promptEngine = req -> {
            PromptResponse res = new PromptResponse();
            res.setRenderedContent("Rendered report prompt");
            res.setStatus("SUCCESS");
            return res;
        };

        String llmJson = "{" +
            "\"reportId\":\"RPT-5678\"," +
            "\"reportVersion\":\"v1.0\"," +
            "\"status\":\"COMPLETED\"," +
            "\"summary\":\"5 tests executed, all passed\"," +
            "\"overallResult\":\"PASS\"," +
            "\"totalTestCases\":5," +
            "\"passedTests\":5," +
            "\"failedTests\":0," +
            "\"passPercentage\":100.0," +
            "\"recommendations\":[\"Add negative test cases\"]," +
            "\"generatedBy\":\"AI-QA-OS ReportingAgent\"" +
            "}";

        LLMProviderManager providerManager = new StubLLMProviderManager(llmJson);

        agent = new ReportingAgent();
        ReflectionTestUtils.setField(agent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(agent, "providerManager", providerManager);

        registry = new AgentRegistry();
        registry.register(UUID.randomUUID(), agent);

        manager = new AgentManagerImpl();
        ReflectionTestUtils.setField(manager, "agentRegistry", registry);

        responseValidator = new LLMResponseValidator();
        ReflectionTestUtils.setField(responseValidator, "objectMapper", new ObjectMapper());

        step = new ReportingStep();
        ReflectionTestUtils.setField(step, "agentManager", manager);
        ReflectionTestUtils.setField(step, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(step, "responseValidator", responseValidator);
    }

    @Test
    public void testReportingStepProducesQAExecutionReport() {
        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        ExecutionResult execResult = new ExecutionResult();
        execResult.setSuccess(true);
        execResult.setPassed(5);
        execResult.setFailed(0);
        context.getQaWorkflowState().setExecutionResult(execResult);

        WorkflowResponse response = step.execute(request, context);

        assertEquals("SUCCESS", response.getStatus());

        QAExecutionReport report = context.getQaWorkflowState().getQaExecutionReport();
        assertNotNull(report);
        assertEquals("RPT-5678", report.getReportId());
        assertEquals("v1.0", report.getReportVersion());
        assertEquals("COMPLETED", report.getStatus());
        assertEquals("PASS", report.getOverallResult());
        assertEquals(5, report.getTotalTestCases());
        assertEquals(5, report.getPassedTests());
        assertEquals(0, report.getFailedTests());
        assertEquals(100.0, report.getPassPercentage(), 0.01);
        assertNotNull(report.getRecommendations());
        assertFalse(report.getRecommendations().isEmpty());
        assertEquals("AI-QA-OS ReportingAgent", report.getGeneratedBy());

        assertEquals(true, context.getVariables().get("reportCompiled"));
        assertEquals("PASS", context.getVariables().get("executionStatus"));
    }

    @Test
    public void testReportingStepFallbackOnAgentFailure() {
        // Replace provider manager with one that throws
        PromptEngine<PromptRequest, PromptResponse> promptEngine = req -> {
            PromptResponse res = new PromptResponse();
            res.setRenderedContent("Rendered report prompt");
            res.setStatus("SUCCESS");
            return res;
        };

        LLMProviderManager faultyProvider = new LLMProviderManager(null, null, null, null, null, null, null) {
            @Override
            public LLMResponse generate(LLMRequest req) {
                throw new RuntimeException("Simulated LLM unavailable");
            }
        };

        ReportingAgent faultyAgent = new ReportingAgent();
        ReflectionTestUtils.setField(faultyAgent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(faultyAgent, "providerManager", faultyProvider);

        AgentRegistry faultyRegistry = new AgentRegistry();
        faultyRegistry.register(UUID.randomUUID(), faultyAgent);

        AgentManagerImpl faultyManager = new AgentManagerImpl();
        ReflectionTestUtils.setField(faultyManager, "agentRegistry", faultyRegistry);

        ReflectionTestUtils.setField(step, "agentManager", faultyManager);

        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        ExecutionResult execResult = new ExecutionResult();
        execResult.setSuccess(false);
        execResult.setPassed(3);
        execResult.setFailed(2);
        context.getQaWorkflowState().setExecutionResult(execResult);

        WorkflowResponse response = step.execute(request, context);

        assertEquals("SUCCESS", response.getStatus());

        QAExecutionReport report = context.getQaWorkflowState().getQaExecutionReport();
        assertNotNull(report);
        assertTrue(report.getReportId().startsWith("REPORT-FALLBACK-"));
        assertEquals("PARTIAL", report.getStatus());
        assertEquals("v1.0", report.getReportVersion());
        assertEquals("AI-QA-OS ReportingAgent (fallback)", report.getGeneratedBy());
        assertEquals(3, report.getPassedTests());
        assertEquals(2, report.getFailedTests());
        assertEquals("FAIL", report.getOverallResult());
    }

    private static class StubLLMProviderManager extends LLMProviderManager {
        private final String textResponse;
        public StubLLMProviderManager(String textResponse) {
            super(null, null, null, null, null, null, null);
            this.textResponse = textResponse;
        }
        @Override
        public LLMResponse generate(LLMRequest request) {
            return new LLMResponse(textResponse, "test-model", new TokenUsage(100, 150), 300L);
        }
    }
}
