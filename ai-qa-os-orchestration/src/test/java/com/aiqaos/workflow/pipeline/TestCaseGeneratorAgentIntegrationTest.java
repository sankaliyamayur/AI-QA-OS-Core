package com.aiqaos.workflow.pipeline;

import com.aiqaos.agent.impl.TestCaseGeneratorAgent;
import com.aiqaos.agent.registry.AgentRegistry;
import com.aiqaos.agent.manager.AgentManagerImpl;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.requirement.RequirementContext;
import com.aiqaos.core.model.QAAnalysisResult;
import com.aiqaos.core.model.GeneratedTestCaseSuite;
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

public class TestCaseGeneratorAgentIntegrationTest {

    private TestCaseGenerationStep step;
    private TestCaseGeneratorAgent agent;
    private AgentRegistry registry;
    private AgentManagerImpl manager;
    private LLMResponseValidator responseValidator;
    private com.aiqaos.core.repository.TestCaseRepository testCaseRepo;
    private com.aiqaos.core.repository.ModuleRepository moduleRepo;

    @BeforeEach
    public void setUp() {
        // 1. Setup Stubs for Agent
        PromptEngine<PromptRequest, PromptResponse> promptEngine = new StubPromptEngine();
        
        // Output json has "testcases" alternative casing and is missing optional description/steps
        String jsonResponse = "{\n" +
                "  \"suiteId\": \"suite-456\",\n" +
                "  \"testcases\": [\n" +
                "    {\n" +
                "      \"id\": \"TC-001\",\n" +
                "      \"name\": \"Verify invalid login credentials\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        LLMProviderManager providerManager = new StubLLMProviderManager(jsonResponse);

        agent = new TestCaseGeneratorAgent();
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

        // 5. Setup persistence — the step resolves the module from the story path
        // and saves the generated cases, so both repositories must be present.
        com.aiqaos.core.entity.ModuleEntity module = new com.aiqaos.core.entity.ModuleEntity();
        module.setId("admin-login");
        module.setRequirementPath("resources/user-stories/Login/US-001.md");

        moduleRepo = org.mockito.Mockito.mock(com.aiqaos.core.repository.ModuleRepository.class);
        org.mockito.Mockito.when(moduleRepo.findAll()).thenReturn(List.of(module));

        testCaseRepo = org.mockito.Mockito.mock(com.aiqaos.core.repository.TestCaseRepository.class);
        org.mockito.Mockito.when(testCaseRepo.findById(org.mockito.ArgumentMatchers.anyString()))
            .thenReturn(java.util.Optional.empty());

        // 6. Setup Step
        step = new TestCaseGenerationStep();
        ReflectionTestUtils.setField(step, "agentManager", manager);
        ReflectionTestUtils.setField(step, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(step, "responseValidator", responseValidator);
        ReflectionTestUtils.setField(step, "testCaseRepo", testCaseRepo);
        ReflectionTestUtils.setField(step, "moduleRepo", moduleRepo);
    }

    @Test
    public void testTestCaseGenerationStepIntegration() {
        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        // The story path identifies which module the generated cases belong to
        RequirementContext reqContext = new RequirementContext();
        reqContext.getAdditionalMetadata().put("storyPath", "resources/user-stories/Login/US-001.md");
        context.getQaWorkflowState().setRequirementContext(reqContext);

        // Set QAAnalysisResult in context
        QAAnalysisResult analysisResult = new QAAnalysisResult();
        analysisResult.setWorkflowId("workflow-123");
        analysisResult.setAnalysisSummary("Login verification details");
        analysisResult.setIdentifiedScenarios(List.of("Scenario 1"));
        analysisResult.setRiskMatrix(Map.of("Auth Bypass", "HIGH"));
        analysisResult.setReadyForTestDesign(true);
        context.getQaWorkflowState().setQaAnalysisResult(analysisResult);

        // Execute Step
        com.aiqaos.core.contract.WorkflowResponse response = step.execute(request, context);

        assertEquals("SUCCESS", response.getStatus());

        AutonomousQAWorkflowState state = context.getQaWorkflowState();
        assertNotNull(state);

        GeneratedTestCaseSuite suite = state.getGeneratedTestCaseSuite();
        assertNotNull(suite);
        assertEquals("suite-456", suite.getSuiteId());
        assertEquals(1, suite.getTestCases().size());

        GeneratedTestCaseSuite.TestCase tc = suite.getTestCases().get(0);
        assertEquals("TC-001", tc.getId());
        assertEquals("Verify invalid login credentials", tc.getName());
        
        // Assert repaired fields
        assertEquals("", tc.getDescription());
        assertEquals("", tc.getExpectedResult());
        assertEquals("MEDIUM", tc.getPriority());
        assertNotNull(tc.getSteps());
        assertTrue(tc.getSteps().isEmpty());
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
