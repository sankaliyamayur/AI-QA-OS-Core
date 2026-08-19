package com.aiqaos.agent.impl;

import com.aiqaos.agent.skill.SkillInstructionService;
import com.aiqaos.agent.skill.SkillLoader;
import com.aiqaos.agent.skill.SkillRegistry;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.contract.PromptRequest;
import com.aiqaos.core.contract.PromptResponse;
import com.aiqaos.core.context.AgentContext;
import com.aiqaos.core.engine.PromptEngine;
import com.aiqaos.provider.manager.LLMProviderManager;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ScriptGenerationSkillIntegrationTest {

    private ScriptGeneratorAgent agent;
    private SkillInstructionService instructionService;
    private AtomicReference<LLMRequest> capturedLLMRequest;

    @BeforeEach
    public void setUp() {
        capturedLLMRequest = new AtomicReference<>();
        
        SkillRegistry registry = new SkillRegistry();
        SkillLoader loader = new SkillLoader();
        instructionService = new SkillInstructionService(registry, loader);

        PromptEngine<PromptRequest, PromptResponse> promptEngine = request -> {
            PromptResponse res = new PromptResponse();
            res.setRenderedContent("Rendered prompt content");
            return res;
        };

        LLMProviderManager providerManager = new LLMProviderManager(null, null, null, null, null, null, null) {
            @Override
            public LLMResponse generate(LLMRequest request) {
                capturedLLMRequest.set(request);
                String jsonBody = "{\"suiteId\":\"s1\",\"scripts\":[{\"scriptId\":\"sc1\",\"testCaseId\":\"TC-1\",\"targetPlatform\":\"WEB\",\"language\":\"JAVASCRIPT\",\"framework\":\"Playwright\",\"code\":\"// code\"}]}";
                return new LLMResponse(jsonBody, "test-model", new TokenUsage(100, 50), 10L);
            }
        };

        agent = new ScriptGeneratorAgent();
        ReflectionTestUtils.setField(agent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(agent, "providerManager", providerManager);
    }

    @Test
    public void testScriptGeneratorWithSkillInstructionServiceInjected() {
        ReflectionTestUtils.setField(agent, "skillInstructionService", instructionService);

        AgentRequest req = new AgentRequest();
        req.setPrompt("{}");
        
        AgentResponse res = agent.execute(req, new AgentContext());

        assertEquals("SUCCESS", res.getStatus());
        assertNotNull(capturedLLMRequest.get());
        String systemPrompt = capturedLLMRequest.get().getSystemPrompt();
        assertNotNull(systemPrompt);
        assertTrue(systemPrompt.contains("=== SKILL INSTRUCTIONS ==="), "System prompt must contain skill instructions header");
        assertTrue(systemPrompt.contains("Automation Script Generation Skill"), "System prompt must contain automation skill body");
        assertTrue(systemPrompt.contains("=== AGENT INSTRUCTIONS ==="), "System prompt must contain agent header");
    }

    @Test
    public void testScriptGeneratorFallbackWithoutSkillInstructionService() {
        // skillInstructionService remains null
        AgentRequest req = new AgentRequest();
        req.setPrompt("{}");

        AgentResponse res = agent.execute(req, new AgentContext());

        assertEquals("SUCCESS", res.getStatus());
        assertNotNull(capturedLLMRequest.get());
        String systemPrompt = capturedLLMRequest.get().getSystemPrompt();
        assertNotNull(systemPrompt);
        assertFalse(systemPrompt.contains("=== SKILL INSTRUCTIONS ==="), "Fallback system prompt must NOT contain skill header");
        assertEquals("You are a Senior QA Automation Script Generator Agent.", systemPrompt, "Must fallback to exact default prompt");
    }
}
