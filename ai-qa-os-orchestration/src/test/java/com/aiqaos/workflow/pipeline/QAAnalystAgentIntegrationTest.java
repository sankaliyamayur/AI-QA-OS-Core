package com.aiqaos.workflow.pipeline;

import com.aiqaos.agent.impl.QAAnalystAgent;
import com.aiqaos.agent.registry.AgentRegistry;
import com.aiqaos.agent.manager.AgentManagerImpl;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.enums.WorkflowStatus;
import com.aiqaos.core.requirement.RequirementContext;
import com.aiqaos.core.requirement.RequirementParser;
import com.aiqaos.core.requirement.RequirementReader;
import com.aiqaos.core.model.QAAnalysisResult;
import com.aiqaos.core.contract.PromptRequest;
import com.aiqaos.core.contract.PromptResponse;
import com.aiqaos.core.engine.PromptEngine;
import com.aiqaos.provider.manager.LLMProviderManager;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class QAAnalystAgentIntegrationTest {

    private RequirementReaderStep readerStep;
    private QAAnalysisStep analysisStep;
    private QAAnalystAgent agent;
    private AgentRegistry registry;
    private AgentManagerImpl manager;

    @BeforeEach
    public void setUp() {
        // 1. Setup Stubs for Requirement Steps
        RequirementReader reader = new StubRequirementReader("Title: login");
        RequirementContext reqContext = new RequirementContext("Title: login");
        reqContext.setTitle("Admin Login Story");
        reqContext.setRawContent("As an admin...");
        RequirementParser parser = new StubRequirementParser(reqContext);

        readerStep = new RequirementReaderStep();
        ReflectionTestUtils.setField(readerStep, "reader", reader);
        ReflectionTestUtils.setField(readerStep, "parser", parser);

        // 2. Setup Stubs for Agent
        PromptEngine<PromptRequest, PromptResponse> promptEngine = new StubPromptEngine();
        String jsonResponse = "{\n" +
                "  \"analysisSummary\": \"LLM parsed overview of story\",\n" +
                "  \"identifiedScenarios\": [\"Admin logs in successfully\", \"Admin enters invalid password\"],\n" +
                "  \"riskMatrix\": {\"Weak Credentials\": \"MEDIUM\", \"Brute Force\": \"HIGH\"},\n" +
                "  \"readyForTestDesign\": true\n" +
                "}";
        LLMProviderManager providerManager = new StubLLMProviderManager(jsonResponse);

        agent = new QAAnalystAgent();
        ReflectionTestUtils.setField(agent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(agent, "providerManager", providerManager);

        // 3. Register Agent in Registry
        registry = new AgentRegistry();
        registry.register(java.util.UUID.randomUUID(), agent);

        // 4. Setup Manager
        manager = new AgentManagerImpl();
        ReflectionTestUtils.setField(manager, "agentRegistry", registry);

        // 5. Setup Step
        analysisStep = new QAAnalysisStep();
        ReflectionTestUtils.setField(analysisStep, "agentManager", manager);
        ReflectionTestUtils.setField(analysisStep, "objectMapper", new ObjectMapper());
    }

    @Test
    public void testReaderToAnalysisStepIntegration() {
        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("storyPath", "US-001.md");
        request.setInputs(inputs);

        // Step 1: Execute reader step
        com.aiqaos.core.contract.WorkflowResponse resReader = readerStep.execute(request, context);
        assertEquals("SUCCESS", resReader.getStatus());

        // Step 2: Execute analysis step (delegates to QAAnalystAgent)
        com.aiqaos.core.contract.WorkflowResponse resAnalysis = analysisStep.execute(request, context);
        assertEquals("SUCCESS", resAnalysis.getStatus());

        // Assert context state is populated by both steps
        AutonomousQAWorkflowState state = context.getQaWorkflowState();
        assertNotNull(state);
        
        // Assert story reading properties
        assertNotNull(state.getRequirementContext());
        assertEquals("Admin Login Story", state.getRequirementContext().getTitle());

        // Assert parsed result from QAAnalystAgent output
        QAAnalysisResult analysisResult = state.getQaAnalysisResult();
        assertNotNull(analysisResult);
        assertEquals("LLM parsed overview of story", analysisResult.getAnalysisSummary());
        assertTrue(analysisResult.isReadyForTestDesign());
        assertEquals(2, analysisResult.getIdentifiedScenarios().size());
        assertEquals("HIGH", analysisResult.getRiskMatrix().get("Brute Force"));
    }

    private static class StubRequirementReader extends RequirementReader {
        private final String content;
        public StubRequirementReader(String content) { this.content = content; }
        @Override
        public String readRequirement(String filePath) throws IOException { return content; }
    }

    private static class StubRequirementParser extends RequirementParser {
        private final RequirementContext context;
        public StubRequirementParser(RequirementContext context) { this.context = context; }
        @Override
        public RequirementContext parse(String rawContent) { return context; }
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
