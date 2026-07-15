package com.aiqaos.agent.impl;

import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.context.AgentContext;
import com.aiqaos.core.enums.AgentStatus;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.contract.PromptRequest;
import com.aiqaos.core.contract.PromptResponse;
import com.aiqaos.core.engine.PromptEngine;
import com.aiqaos.provider.manager.LLMProviderManager;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

public class QAAnalystAgentTest {

    @Test
    public void testQAAnalystAgentExecutionSuccess() {
        QAAnalystAgent agent = new QAAnalystAgent();

        assertEquals(AgentType.QA_ENGINEER, agent.getType());
        assertEquals(AgentStatus.IDLE, agent.getStatus());

        // Setup stubs using ReflectionTestUtils
        StubPromptEngine stubPromptEngine = new StubPromptEngine("Rendered Pebble Template");
        StubLLMProviderManager stubLLMProvider = new StubLLMProviderManager("{\"analysisSummary\":\"Login story verified\"}");
        com.aiqaos.agent.config.AgentPropertiesConfig propertiesConfig = new com.aiqaos.agent.config.AgentPropertiesConfig();

        ReflectionTestUtils.setField(agent, "promptEngine", stubPromptEngine);
        ReflectionTestUtils.setField(agent, "providerManager", stubLLMProvider);
        ReflectionTestUtils.setField(agent, "agentPropertiesConfig", propertiesConfig);

        AgentRequest request = new AgentRequest();
        request.setPrompt("As an admin, I want to login...");
        request.setSystemInstruction("Override instruction");

        AgentResponse response = agent.execute(request, new AgentContext());

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("{\"analysisSummary\":\"Login story verified\"}", response.getContent());
        assertEquals(AgentStatus.IDLE, agent.getStatus());
        
        // Assert parameters passed to stubs correctly
        assertEquals("QA_ANALYSIS", stubPromptEngine.lastRequestedTemplate);
        assertEquals("As an admin, I want to login...", stubPromptEngine.lastParams.get("story"));
        assertEquals("Rendered Pebble Template", stubLLMProvider.lastRequestedPrompt);
        assertEquals("Override instruction", stubLLMProvider.lastRequestedSystemPrompt);
    }

    private static class StubPromptEngine implements PromptEngine<PromptRequest, PromptResponse> {
        private final String renderValue;
        String lastRequestedTemplate;
        java.util.Map<String, Object> lastParams;

        public StubPromptEngine(String renderValue) {
            this.renderValue = renderValue;
        }

        @Override
        public PromptResponse renderPrompt(PromptRequest request) {
            this.lastRequestedTemplate = request.getTemplateName();
            this.lastParams = request.getParameters();
            
            PromptResponse res = new PromptResponse();
            res.setRenderedContent(renderValue);
            res.setStatus("SUCCESS");
            return res;
        }
    }

    private static class StubLLMProviderManager extends LLMProviderManager {
        private final String textResponse;
        String lastRequestedPrompt;
        String lastRequestedSystemPrompt;

        public StubLLMProviderManager(String textResponse) {
            super(null, null, null, null, null, null, null);
            this.textResponse = textResponse;
        }

        @Override
        public LLMResponse generate(LLMRequest request) {
            this.lastRequestedPrompt = request.getPrompt();
            this.lastRequestedSystemPrompt = request.getSystemPrompt();
            return new LLMResponse(
                textResponse,
                "mock-model",
                new TokenUsage(50, 100),
                250L
            );
        }
    }
}
