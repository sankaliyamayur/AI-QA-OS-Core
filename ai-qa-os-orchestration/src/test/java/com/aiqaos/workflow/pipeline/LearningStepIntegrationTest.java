package com.aiqaos.workflow.pipeline;

import com.aiqaos.agent.impl.LearningAgent;
import com.aiqaos.agent.registry.AgentRegistry;
import com.aiqaos.agent.manager.AgentManagerImpl;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.QAExecutionReport;
import com.aiqaos.core.model.LearningResult;
import com.aiqaos.core.model.FailurePattern;
import com.aiqaos.core.contract.PromptRequest;
import com.aiqaos.core.contract.PromptResponse;
import com.aiqaos.core.engine.PromptEngine;
import com.aiqaos.provider.manager.LLMProviderManager;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import com.aiqaos.workflow.validation.LLMResponseValidator;
import com.aiqaos.learning.engine.LearningEngineImpl;
import com.aiqaos.learning.analyzer.FailurePatternAnalyzer;
import com.aiqaos.learning.healing.SelfHealingEngine;
import com.aiqaos.learning.memory.LearningMemoryStore;
import com.aiqaos.memory.store.MemoryStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LearningStepIntegrationTest {

    private LearningStep step;
    private LearningEngineImpl engine;
    private LearningAgent agent;
    private LearningMemoryStore learningMemoryStore;
    private StubMemoryStore rawMemoryStore;

    @BeforeEach
    public void setUp() {
        PromptEngine<PromptRequest, PromptResponse> promptEngine = req -> {
            PromptResponse res = new PromptResponse();
            res.setRenderedContent("Rendered Learning template prompt");
            res.setStatus("SUCCESS");
            return res;
        };

        String llmJson = "{" +
                "\"patterns\":[{\"patternId\":\"PAT-TEST\",\"errorType\":\"LOCATOR_ERROR\",\"rootCause\":\"Selector modified\",\"impactedComponent\":\"Grid\",\"occurrenceCount\":1,\"confidence\":0.90}]," +
                "\"recommendations\":[{\"recommendationId\":\"REC-TEST\",\"issue\":\"Selector mismatch\",\"suggestedAction\":\"Modify xpath\",\"actionType\":\"LOCATOR_UPDATE\",\"regenerateScript\":false,\"updateLocator\":true,\"updatePrompt\":false,\"confidence\":0.92}]," +
                "\"events\":[]" +
                "}";

        LLMProviderManager providerManager = new StubLLMProviderManager(llmJson);

        agent = new LearningAgent();
        ReflectionTestUtils.setField(agent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(agent, "providerManager", providerManager);

        AgentRegistry registry = new AgentRegistry();
        registry.register(UUID.randomUUID(), agent);

        AgentManagerImpl manager = new AgentManagerImpl();
        ReflectionTestUtils.setField(manager, "agentRegistry", registry);

        LLMResponseValidator validator = new LLMResponseValidator();
        ReflectionTestUtils.setField(validator, "objectMapper", new ObjectMapper());

        rawMemoryStore = new StubMemoryStore();
        learningMemoryStore = new LearningMemoryStore();
        ReflectionTestUtils.setField(learningMemoryStore, "memoryStore", rawMemoryStore);

        FailurePatternAnalyzer analyzer = new FailurePatternAnalyzer();
        SelfHealingEngine healing = new SelfHealingEngine();

        engine = new LearningEngineImpl();
        ReflectionTestUtils.setField(engine, "agentManager", manager);
        ReflectionTestUtils.setField(engine, "analyzer", analyzer);
        ReflectionTestUtils.setField(engine, "healingEngine", healing);
        ReflectionTestUtils.setField(engine, "memoryStore", learningMemoryStore);
        ReflectionTestUtils.setField(engine, "objectMapper", new ObjectMapper());

        step = new LearningStep();
        ReflectionTestUtils.setField(step, "learningEngine", engine);
        ReflectionTestUtils.setField(step, "responseValidator", validator);
        ReflectionTestUtils.setField(step, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(step, "memoryStore", learningMemoryStore);
    }

    @Test
    public void testLearningStepExecutesAndSavesState() {
        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        QAExecutionReport report = new QAExecutionReport();
        report.setOverallResult("FAIL");
        ExecutionResult exec = new ExecutionResult();
        exec.setSuccess(false);
        exec.setFailed(1);
        report.setExecutionResult(exec);

        BugAnalysisReport bug = new BugAnalysisReport();
        bug.setFailureCategory("LOCATOR_ERROR");
        bug.setRootCause("Selector modified");
        bug.setSelfHealingSuggestion("Modify xpath");
        bug.setConfidence(0.90);
        report.setBugAnalysisReport(bug);

        context.getQaWorkflowState().setQaExecutionReport(report);

        WorkflowResponse response = step.execute(request, context);

        assertEquals("SUCCESS", response.getStatus());

        LearningResult result = context.getQaWorkflowState().getLearningResult();
        assertNotNull(result);
        assertEquals(1, result.getPatterns().size());
        assertEquals("PAT-TEST", result.getPatterns().get(0).getPatternId());
        assertEquals(1, result.getRecommendations().size());
        assertEquals("REC-TEST", result.getRecommendations().get(0).getRecommendationId());

        // Verify stored in memory store
        assertFalse(learningMemoryStore.getFailurePatterns().isEmpty());
    }

    private static class StubLLMProviderManager extends LLMProviderManager {
        private final String textResponse;
        public StubLLMProviderManager(String textResponse) {
            super(null, null, null, null, null, null, null);
            this.textResponse = textResponse;
        }
        @Override
        public LLMResponse generate(LLMRequest request) {
            return new LLMResponse(textResponse, "test-model", new TokenUsage(100, 150), 300L);
        }
    }

    private static class StubMemoryStore implements MemoryStore {
        private final java.util.Map<String, Object> db = new HashMap<>();
        @Override
        public void put(String key, Object value, Duration ttl) { db.put(key, value); }
        @Override
        public Optional<Object> get(String key) { return Optional.ofNullable(db.get(key)); }
        @Override
        public void remove(String key) { db.remove(key); }
        @Override
        public void clear() { db.clear(); }
    }
}
