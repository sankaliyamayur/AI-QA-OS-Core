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

public class SelfHealingAgentTest {

    private SelfHealingAgent agent;

    @BeforeEach
    void setUp() {
        agent = new SelfHealingAgent();

        PromptEngine<PromptRequest, PromptResponse> promptEngine = req -> {
            assertEquals("SELF_HEALING", req.getTemplateName());
            PromptResponse res = new PromptResponse();
            res.setRenderedContent("Rendered SelfHealing context: " + req.getParameters().get("context"));
            return res;
        };

        LLMProviderManager providerManager = new LLMProviderManager(null, null, null, null, null, null, null) {
            @Override
            public LLMResponse generate(LLMRequest llmReq) {
                assertEquals("SELF_HEALING", llmReq.getPurpose());
                assertEquals("SELF_HEALING_ENGINEER", llmReq.getAgentType());
                return new LLMResponse(
                    "{\"healingAction\":\"LOCATOR_UPDATE\",\"reason\":\"Selector mismatched\",\"confidence\":0.95,\"retryRequired\":true,\"scriptRegenerationRequired\":false}",
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
    void testSelfHealingAgentTypeIsSelfHealingEngineer() {
        assertEquals(AgentType.SELF_HEALING_ENGINEER, agent.getType());
    }

    @Test
    void testSelfHealingAgentReturnsDecisionResponse() {
        AgentRequest request = new AgentRequest();
        request.setPrompt("{\"execution\":{\"success\":false}}");

        AgentResponse response = agent.execute(request, new AgentContext());

        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getContent());
        assertTrue(response.getContent().contains("healingAction"));
    }
}
