package com.aiqaos.agent.impl;

import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.contract.PromptRequest;
import com.aiqaos.core.contract.PromptResponse;
import com.aiqaos.core.context.AgentContext;
import com.aiqaos.core.engine.PromptEngine;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.provider.manager.LLMProviderManager;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class ReportingAgentTest {

    private ReportingAgent agent;

    @BeforeEach
    void setUp() {
        agent = new ReportingAgent();

        PromptEngine<PromptRequest, PromptResponse> promptEngine = req -> {
            assertEquals("REPORT_GENERATION", req.getTemplateName());
            PromptResponse res = new PromptResponse();
            res.setRenderedContent("Generate a QA report for: " + req.getParameters().get("workflowData"));
            return res;
        };

        LLMProviderManager providerManager = new LLMProviderManager(null, null, null, null, null, null, null) {
            @Override
            public LLMResponse generate(LLMRequest llmReq) {
                assertEquals("REPORT_GENERATION", llmReq.getPurpose());
                assertEquals("REPORTER", llmReq.getAgentType());
                return new LLMResponse(
                    "{\"reportId\":\"RPT-001\",\"reportVersion\":\"v1.0\",\"status\":\"COMPLETED\"," +
                    "\"summary\":\"All 5 tests passed\",\"overallResult\":\"PASS\"," +
                    "\"totalTestCases\":5,\"passedTests\":5,\"failedTests\":0," +
                    "\"passPercentage\":100.0,\"recommendations\":[\"Increase coverage\"]," +
                    "\"generatedBy\":\"AI-QA-OS ReportingAgent\"}",
                    "mock-model",
                    new TokenUsage(50, 100),
                    250L
                );
            }
        };

        ReflectionTestUtils.setField(agent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(agent, "providerManager", providerManager);
    }

    @Test
    void testReportingAgentTypeIsReporter() {
        assertEquals(AgentType.REPORTER, agent.getType());
    }

    @Test
    void testReportingAgentReturnsSuccessResponse() {
        AgentRequest request = new AgentRequest();
        request.setPrompt("{\"execution\":{\"passed\":5,\"failed\":0}}");

        AgentResponse response = agent.execute(request, new AgentContext());

        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getContent());
        assertTrue(response.getContent().contains("RPT-001"));
        assertTrue(response.getContent().contains("COMPLETED"));
        assertEquals(0.95, response.getConfidenceScore(), 0.001);
    }
}
