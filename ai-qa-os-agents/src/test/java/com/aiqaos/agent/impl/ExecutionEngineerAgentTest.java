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

public class ExecutionEngineerAgentTest {

    @Test
    public void testExecutionEngineerAgentExecutionSuccess() {
        ExecutionEngineerAgent agent = new ExecutionEngineerAgent();

        assertEquals(AgentType.EXECUTION_ENGINEER, agent.getType());
        assertEquals(AgentStatus.IDLE, agent.getStatus());

        StubPromptEngine stubPromptEngine = new StubPromptEngine("Rendered Execution Strategy Prompt");
        StubLLMProviderManager stubLLMProvider = new StubLLMProviderManager("{\"executionMode\":\"SEQUENTIAL\", \"parallel\":false, \"environment\":\"DEV\", \"timeout\":30000, \"retryCount\":2, \"browser\":\"CHROME\", \"headless\":true}");

        ReflectionTestUtils.setField(agent, "promptEngine", stubPromptEngine);
        ReflectionTestUtils.setField(agent, "providerManager", stubLLMProvider);

        AgentRequest request = new AgentRequest();
        request.setPrompt("JSON generated scripts context");

        AgentResponse response = agent.execute(request, new AgentContext());

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertTrue(response.getContent().contains("\"executionMode\":\"SEQUENTIAL\""));
        assertEquals(AgentStatus.IDLE, agent.getStatus());

        assertEquals("EXECUTION", stubPromptEngine.lastRequestedTemplate);
        assertEquals("JSON generated scripts context", stubPromptEngine.lastParams.get("scripts"));
        assertEquals("Rendered Execution Strategy Prompt", stubLLMProvider.lastRequestedPrompt);
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

        public StubLLMProviderManager(String textResponse) {
            super(null, null, null, null, null, null, null);
            this.textResponse = textResponse;
        }

        @Override
        public LLMResponse generate(LLMRequest request) {
            this.lastRequestedPrompt = request.getPrompt();
            return new LLMResponse(
                textResponse,
                "mock-model",
                new TokenUsage(50, 100),
                250L
            );
        }
    }
}
