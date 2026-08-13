package com.aiqaos.agent.prompt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aiqaos.agent.config.AgentPropertiesConfig;
import com.aiqaos.agent.impl.QAAnalystAgent;
import com.aiqaos.agent.impl.ScriptGeneratorAgent;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.PromptRequest;
import com.aiqaos.core.contract.PromptResponse;
import com.aiqaos.core.engine.PromptEngine;
import com.aiqaos.provider.manager.LLMProviderManager;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AgentTokenBudgetConfigTest {

    @Test
    @DisplayName("QAAnalystAgent sends configured 4096 maxTokens budget")
    void qaAnalystAgentSendsConfiguredTokenBudget() throws Exception {
        QAAnalystAgent agent = new QAAnalystAgent();

        LLMProviderManager providerManager = mock(LLMProviderManager.class);
        LLMResponse okResponse = new LLMResponse("{\"analysisSummary\":\"OK\"}", "gemini-1.5-flash", new TokenUsage(10, 10), 100);
        when(providerManager.generate(any(LLMRequest.class))).thenReturn(okResponse);

        PromptEngine<PromptRequest, PromptResponse> promptEngine = mock(PromptEngine.class);
        PromptResponse pResp = new PromptResponse();
        pResp.setRenderedContent("Requirement: test");
        when(promptEngine.renderPrompt(any(PromptRequest.class))).thenReturn(pResp);

        AgentPropertiesConfig config = new AgentPropertiesConfig();
        config.setMaxTokens(Map.of("qa-analysis", 4096));

        setField(agent, "providerManager", providerManager);
        setField(agent, "promptEngine", promptEngine);
        setField(agent, "agentPropertiesConfig", config);

        AgentRequest req = new AgentRequest();
        req.setPrompt("Test story");

        try {
            agent.execute(req, null);
        } catch (Exception ignored) {
            // responseValidator may be missing in unit test stub, we check LLMRequest maxTokens
        }

        ArgumentCaptor<LLMRequest> captor = ArgumentCaptor.forClass(LLMRequest.class);
        verify(providerManager).generate(captor.capture());
        assertEquals(4096, captor.getValue().getMaxTokens(), "QAAnalystAgent should request 4096 maxTokens");
    }

    @Test
    @DisplayName("ScriptGeneratorAgent sends configured 8192 maxTokens budget")
    void scriptGeneratorAgentSendsConfiguredTokenBudget() throws Exception {
        ScriptGeneratorAgent agent = new ScriptGeneratorAgent();

        LLMProviderManager providerManager = mock(LLMProviderManager.class);
        LLMResponse okResponse = new LLMResponse("{\"scripts\":[]}", "gemini-1.5-flash", new TokenUsage(10, 10), 100);
        when(providerManager.generate(any(LLMRequest.class))).thenReturn(okResponse);

        PromptEngine<PromptRequest, PromptResponse> promptEngine = mock(PromptEngine.class);
        PromptResponse pResp = new PromptResponse();
        pResp.setRenderedContent("Test cases: []");
        when(promptEngine.renderPrompt(any(PromptRequest.class))).thenReturn(pResp);

        AgentPropertiesConfig config = new AgentPropertiesConfig();
        config.setMaxTokens(Map.of("script-generation", 8192));

        setField(agent, "providerManager", providerManager);
        setField(agent, "promptEngine", promptEngine);
        setField(agent, "agentPropertiesConfig", config);

        AgentRequest req = new AgentRequest();
        req.setPrompt("Test cases");

        try {
            agent.execute(req, null);
        } catch (Exception ignored) {
        }

        ArgumentCaptor<LLMRequest> captor = ArgumentCaptor.forClass(LLMRequest.class);
        verify(providerManager).generate(captor.capture());
        assertEquals(8192, captor.getValue().getMaxTokens(), "ScriptGeneratorAgent should request 8192 maxTokens");
    }

    private static void setField(Object target, String fieldName, Object val) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, val);
    }
}
