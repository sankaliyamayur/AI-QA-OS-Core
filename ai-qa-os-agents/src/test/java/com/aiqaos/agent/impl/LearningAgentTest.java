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

public class LearningAgentTest {

    private LearningAgent agent;

    @BeforeEach
    void setUp() {
        agent = new LearningAgent();

        PromptEngine<PromptRequest, PromptResponse> promptEngine = req -> {
            assertEquals("LEARNING_ENGINE", req.getTemplateName());
            PromptResponse res = new PromptResponse();
            res.setRenderedContent("Rendered Learning prompt context: " + req.getParameters().get("context"));
            return res;
        };

        LLMProviderManager providerManager = new LLMProviderManager(null, null, null, null, null, null, null) {
            @Override
            public LLMResponse generate(LLMRequest llmReq) {
                assertEquals("LEARNING_ENGINE", llmReq.getPurpose());
                assertEquals("LEARNING_ENGINEER", llmReq.getAgentType());
                return new LLMResponse(
                    "{\"patterns\":[],\"recommendations\":[],\"events\":[]}",
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
    void testLearningAgentTypeIsLearningEngineer() {
        assertEquals(AgentType.LEARNING_ENGINEER, agent.getType());
    }

    @Test
    void testLearningAgentReturnsSuccessResponse() {
        AgentRequest request = new AgentRequest();
        request.setPrompt("{\"execution\":{\"passed\":5,\"failed\":0}}");

        AgentResponse response = agent.execute(request, new AgentContext());

        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getContent());
        assertTrue(response.getContent().contains("patterns"));
    }
}
