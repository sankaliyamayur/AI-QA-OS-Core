package com.aiqaos.workflow.pipeline;

import com.aiqaos.agent.impl.ExecutionEngineerAgent;
import com.aiqaos.agent.registry.AgentRegistry;
import com.aiqaos.agent.manager.AgentManagerImpl;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.GeneratedScriptSuite.AutomationScript;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.contract.PromptRequest;
import com.aiqaos.core.contract.PromptResponse;
import com.aiqaos.core.engine.PromptEngine;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
import com.aiqaos.execution.engine.PlaywrightExecutionEngine;
import com.aiqaos.provider.manager.LLMProviderManager;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ExecutionStepIntegrationTest {

    private ExecutionStep step;
    private ExecutionEngineerAgent agent;
    private AgentRegistry registry;
    private AgentManagerImpl manager;
    private PlaywrightExecutionEngine playwrightEngine;
    private ExecutionEngineFactory factory;

    @BeforeEach
    public void setUp() {
        // 1. Setup Stubs for Agent
        PromptEngine<PromptRequest, PromptResponse> promptEngine = new StubPromptEngine();
        String jsonResponse = "{\n" +
                "  \"executionMode\": \"SEQUENTIAL\",\n" +
                "  \"environment\": \"STAGE\",\n" +
                "  \"timeout\": 45000,\n" +
                "  \"retryCount\": 3,\n" +
                "  \"browser\": \"FIREFOX\",\n" +
                "  \"headless\": false\n" +
                "}";
        LLMProviderManager providerManager = new StubLLMProviderManager(jsonResponse);

        agent = new ExecutionEngineerAgent();
        ReflectionTestUtils.setField(agent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(agent, "providerManager", providerManager);

        // 2. Register Agent
        registry = new AgentRegistry();
        registry.register(java.util.UUID.randomUUID(), agent);

        // 3. Setup Manager
        manager = new AgentManagerImpl();
        ReflectionTestUtils.setField(manager, "agentRegistry", registry);

        // 4. Setup Execution Engines and Factory
        playwrightEngine = new PlaywrightExecutionEngine();
        factory = new ExecutionEngineFactory(List.of(playwrightEngine));

        // 5. Setup Step
        step = new ExecutionStep();
        ReflectionTestUtils.setField(step, "agentManager", manager);
        ReflectionTestUtils.setField(step, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(step, "engineFactory", factory);
    }

    @Test
    public void testExecutionStepIntegration() {
        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        // Set GeneratedScriptSuite in context
        GeneratedScriptSuite scriptSuite = new GeneratedScriptSuite();
        scriptSuite.setSuiteId("script-suite-999");
        AutomationScript script = new AutomationScript();
        script.setScriptId("script-1");
        script.setTestCaseId("TC-1");
        script.setFramework("Playwright");
        script.setLanguage("JAVASCRIPT");
        script.setCode("await page.goto('https://example.com');");
        scriptSuite.setScripts(List.of(script));

        context.getQaWorkflowState().setGeneratedScriptSuite(scriptSuite);

        // Execute Step
        com.aiqaos.core.contract.WorkflowResponse response = step.execute(request, context);

        assertEquals("SUCCESS", response.getStatus());

        AutonomousQAWorkflowState state = context.getQaWorkflowState();
        assertNotNull(state);

        ExecutionResult execResult = state.getExecutionResult();
        assertNotNull(execResult);
        assertTrue(execResult.isSuccess());
        assertEquals("PASSED", execResult.getStatus());
        assertEquals(1, execResult.getPassed());
        assertEquals(0, execResult.getFailed());
        assertNotNull(execResult.getStartTime());
        assertNotNull(execResult.getEndTime());
        assertTrue(execResult.getDuration() >= 0);
        
        // Assert rich metadata
        assertNotNull(execResult.getConsoleLogs());
        assertTrue(execResult.getConsoleLogs().contains("Launching browser: FIREFOX"));
        assertTrue(execResult.getConsoleLogs().contains("Running in headless mode: false"));
        assertEquals("/reports/playwright-report.html", execResult.getReportLocation());
        assertEquals(1, execResult.getScreenshots().size());
        assertEquals("/artifacts/screenshot-1.png", execResult.getScreenshots().get(0));
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
