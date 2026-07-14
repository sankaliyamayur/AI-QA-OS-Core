package com.aiqaos.workflow.pipeline;

import com.aiqaos.agent.impl.ScriptGeneratorAgent;
import com.aiqaos.agent.registry.AgentRegistry;
import com.aiqaos.agent.manager.AgentManagerImpl;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.model.GeneratedTestCaseSuite;
import com.aiqaos.core.model.GeneratedScriptSuite;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class ScriptGeneratorAgentIntegrationTest {

    private ScriptGenerationStep step;
    private ScriptGeneratorAgent agent;
    private AgentRegistry registry;
    private AgentManagerImpl manager;
    private LLMResponseValidator responseValidator;

    @BeforeEach
    public void setUp() {
        // 1. Setup Stubs for Agent
        PromptEngine<PromptRequest, PromptResponse> promptEngine = new StubPromptEngine();
        
        // Output json has "scriptsuite" casing and is missing optional code / framework
        String jsonResponse = "{\n" +
                "  \"suiteId\": \"suite-789\",\n" +
                "  \"scriptsuite\": [\n" +
                "    {\n" +
                "      \"scriptID\": \"script-101\",\n" +
                "      \"testcaseId\": \"TC-001\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        LLMProviderManager providerManager = new StubLLMProviderManager(jsonResponse);

        agent = new ScriptGeneratorAgent();
        ReflectionTestUtils.setField(agent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(agent, "providerManager", providerManager);

        // 2. Register Agent
        registry = new AgentRegistry();
        registry.register(java.util.UUID.randomUUID(), agent);

        // 3. Setup Manager
        manager = new AgentManagerImpl();
        ReflectionTestUtils.setField(manager, "agentRegistry", registry);

        // 4. Setup Validator
        responseValidator = new LLMResponseValidator();
        ReflectionTestUtils.setField(responseValidator, "objectMapper", new ObjectMapper());

        // 5. Setup Step
        step = new ScriptGenerationStep();
        ReflectionTestUtils.setField(step, "agentManager", manager);
        ReflectionTestUtils.setField(step, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(step, "responseValidator", responseValidator);
    }

    @Test
    public void testScriptGenerationStepIntegration() {
        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        // Set GeneratedTestCaseSuite in context
        GeneratedTestCaseSuite tcSuite = new GeneratedTestCaseSuite();
        tcSuite.setSuiteId("tc-suite-123");
        GeneratedTestCaseSuite.TestCase tc = new GeneratedTestCaseSuite.TestCase();
        tc.setId("TC-001");
        tc.setName("Verify user authentication");
        tcSuite.setTestCases(List.of(tc));
        context.getQaWorkflowState().setGeneratedTestCaseSuite(tcSuite);

        // Execute Step
        com.aiqaos.core.contract.WorkflowResponse response = step.execute(request, context);

        assertEquals("SUCCESS", response.getStatus());

        AutonomousQAWorkflowState state = context.getQaWorkflowState();
        assertNotNull(state);

        GeneratedScriptSuite scriptSuite = state.getGeneratedScriptSuite();
        assertNotNull(scriptSuite);
        assertEquals("suite-789", scriptSuite.getSuiteId());
        assertEquals(1, scriptSuite.getScripts().size());

        GeneratedScriptSuite.AutomationScript script = scriptSuite.getScripts().get(0);
        assertEquals("script-101", script.getScriptId());
        assertEquals("TC-001", script.getTestCaseId());
        assertEquals("Playwright", script.getFramework());
        assertEquals("JAVASCRIPT", script.getLanguage());
        assertEquals("WEB", script.getTargetPlatform());
        assertEquals("throw new UnsupportedOperationException(\"LLM did not generate automation code.\");", script.getCode());
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
