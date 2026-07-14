package com.aiqaos.workflow.pipeline;

import com.aiqaos.agent.impl.QAAnalystAgent;
import com.aiqaos.agent.impl.TestCaseGeneratorAgent;
import com.aiqaos.agent.impl.ScriptGeneratorAgent;
import com.aiqaos.agent.impl.ExecutionEngineerAgent;
import com.aiqaos.agent.impl.BugAnalyzerAgent;
import com.aiqaos.agent.impl.ReportingAgent;
import com.aiqaos.agent.impl.LearningAgent;
import com.aiqaos.agent.impl.SelfHealingAgent;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.agent.registry.AgentRegistry;
import com.aiqaos.agent.manager.AgentManagerImpl;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
import com.aiqaos.execution.engine.PlaywrightExecutionEngine;
import com.aiqaos.workflow.validation.LLMResponseValidator;
import com.aiqaos.memory.store.MemoryStore;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.enums.WorkflowStatus;
import com.aiqaos.core.requirement.RequirementContext;
import com.aiqaos.core.requirement.RequirementParser;
import com.aiqaos.core.requirement.RequirementReader;
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
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class AutonomousQAPipelineTest {

    private StubRequirementReader reader;
    private StubRequirementParser parser;

    private RequirementReaderStep requirementReaderStep;
    private QAAnalysisStep qaAnalysisStep;
    private TestCaseGenerationStep testCaseGenerationStep;
    private ScriptGenerationStep scriptGenerationStep;
    private ExecutionStep executionStep;
    private BugAnalysisStep bugAnalysisStep;
    private ReportingStep reportingStep;
    private LearningStep learningStep;
    private SelfHealingStep selfHealingStep;

    private AutonomousQAPipelineOrchestrator orchestrator;

    @BeforeEach
    public void setUp() {
        reader = new StubRequirementReader("Title: test story");
        
        RequirementContext reqContext = new RequirementContext("Title: test story");
        reqContext.setTitle("Test Title");
        reqContext.setDescription("As a test...");
        reqContext.getAcceptanceCriteria().add("Criteria 1");
        parser = new StubRequirementParser(reqContext);

        requirementReaderStep = new RequirementReaderStep();
        ReflectionTestUtils.setField(requirementReaderStep, "reader", reader);
        ReflectionTestUtils.setField(requirementReaderStep, "parser", parser);

        // Setup AgentManager with QAAnalystAgent and ObjectMapper
        PromptEngine<PromptRequest, PromptResponse> promptEngine = new StubPromptEngine();
        String jsonResponse = "{\n" +
                "  \"analysisSummary\": \"Analyzed story: Test Title\",\n" +
                "  \"identifiedScenarios\": [\"Verify login details\", \"Forgot password workflow\"],\n" +
                "  \"riskMatrix\": {\"Authentication Bypass\": \"HIGH\"},\n" +
                "  \"readyForTestDesign\": true\n" +
                "}";
        LLMProviderManager providerManager = new StubLLMProviderManager(jsonResponse);

        QAAnalystAgent agent = new QAAnalystAgent();
        ReflectionTestUtils.setField(agent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(agent, "providerManager", providerManager);

        // Setup TestCaseGeneratorAgent
        String testCasesJsonResponse = "{\n" +
                "  \"suiteId\": \"suite-test\",\n" +
                "  \"testCases\": [\n" +
                "    {\n" +
                "      \"id\": \"TC-1\",\n" +
                "      \"name\": \"Verify login details\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"TC-2\",\n" +
                "      \"name\": \"Forgot password workflow\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        LLMProviderManager tcProviderManager = new StubLLMProviderManager(testCasesJsonResponse);
        TestCaseGeneratorAgent tcAgent = new TestCaseGeneratorAgent();
        ReflectionTestUtils.setField(tcAgent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(tcAgent, "providerManager", tcProviderManager);

        // Setup ScriptGeneratorAgent
        String scriptsJsonResponse = "{\n" +
                "  \"suiteId\": \"suite-scripts\",\n" +
                "  \"scripts\": [\n" +
                "    {\n" +
                "      \"scriptId\": \"script-1\",\n" +
                "      \"testCaseId\": \"TC-1\",\n" +
                "      \"targetPlatform\": \"WEB\",\n" +
                "      \"language\": \"JAVASCRIPT\",\n" +
                "      \"framework\": \"Playwright\",\n" +
                "      \"code\": \"// Playwright test code\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"scriptId\": \"script-2\",\n" +
                "      \"testCaseId\": \"TC-2\",\n" +
                "      \"targetPlatform\": \"WEB\",\n" +
                "      \"language\": \"JAVASCRIPT\",\n" +
                "      \"framework\": \"Playwright\",\n" +
                "      \"code\": \"// Playwright test code\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        LLMProviderManager scriptsProviderManager = new StubLLMProviderManager(scriptsJsonResponse);
        ScriptGeneratorAgent scriptsAgent = new ScriptGeneratorAgent();
        ReflectionTestUtils.setField(scriptsAgent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(scriptsAgent, "providerManager", scriptsProviderManager);

        // Setup ExecutionEngineerAgent
        String execStrategyJsonResponse = "{\n" +
                "  \"executionMode\": \"SEQUENTIAL\",\n" +
                "  \"environment\": \"DEV\",\n" +
                "  \"timeout\": 30000,\n" +
                "  \"retryCount\": 2,\n" +
                "  \"browser\": \"CHROME\",\n" +
                "  \"headless\": true\n" +
                "}";
        LLMProviderManager execProviderManager = new StubLLMProviderManager(execStrategyJsonResponse);
        ExecutionEngineerAgent execAgent = new ExecutionEngineerAgent();
        ReflectionTestUtils.setField(execAgent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(execAgent, "providerManager", execProviderManager);

        // Setup BugAnalyzerAgent
        String bugAnalysisJsonResponse = "{\n" +
                "  \"reportId\": \"bug-123\",\n" +
                "  \"rootCause\": \"Element not found selector #submit\",\n" +
                "  \"failureCategory\": \"ELEMENT_NOT_FOUND\",\n" +
                "  \"impactedComponent\": \"Login\",\n" +
                "  \"confidence\": 0.95,\n" +
                "  \"severity\": \"HIGH\",\n" +
                "  \"priority\": \"P1\",\n" +
                "  \"status\": \"OPEN\",\n" +
                "  \"selfHealingSuggestion\": \"Change selector\",\n" +
                "  \"requiresRegeneration\": true\n" +
                "}";
        LLMProviderManager bugProviderManager = new StubLLMProviderManager(bugAnalysisJsonResponse);
        BugAnalyzerAgent bugAgent = new BugAnalyzerAgent();
        ReflectionTestUtils.setField(bugAgent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(bugAgent, "providerManager", bugProviderManager);

        // Setup ReportingAgent
        String reportingJsonResponse = "{\n" +
                "  \"reportId\": \"RPT-001\",\n" +
                "  \"reportVersion\": \"v1.0\",\n" +
                "  \"status\": \"COMPLETED\",\n" +
                "  \"summary\": \"Pipeline executed successfully\",\n" +
                "  \"overallResult\": \"PASS\",\n" +
                "  \"totalTestCases\": 2,\n" +
                "  \"passedTests\": 2,\n" +
                "  \"failedTests\": 0,\n" +
                "  \"passPercentage\": 100.0,\n" +
                "  \"recommendations\": [\"Add edge case tests\"],\n" +
                "  \"generatedBy\": \"AI-QA-OS ReportingAgent\"\n" +
                "}";
        LLMProviderManager reportProviderManager = new StubLLMProviderManager(reportingJsonResponse);
        ReportingAgent reportingAgent = new ReportingAgent();
        ReflectionTestUtils.setField(reportingAgent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(reportingAgent, "providerManager", reportProviderManager);

        // Setup LearningAgent
        String learningJsonResponse = "{\n" +
                "  \"patterns\": [],\n" +
                "  \"recommendations\": [],\n" +
                "  \"events\": []\n" +
                "}";
        LLMProviderManager learningProviderManager = new StubLLMProviderManager(learningJsonResponse);
        LearningAgent learningAgent = new LearningAgent();
        ReflectionTestUtils.setField(learningAgent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(learningAgent, "providerManager", learningProviderManager);

        // Setup SelfHealingAgent mock register
        String selfHealingJsonResponse = "{\n" +
                "  \"healingAction\": \"LOCATOR_UPDATE\",\n" +
                "  \"reason\": \"Test execution repair\",\n" +
                "  \"confidence\": 0.95,\n" +
                "  \"retryRequired\": true,\n" +
                "  \"scriptRegenerationRequired\": false\n" +
                "}";
        LLMProviderManager selfHealingProviderManager = new StubLLMProviderManager(selfHealingJsonResponse);
        SelfHealingAgent selfHealingAgent = new SelfHealingAgent();
        ReflectionTestUtils.setField(selfHealingAgent, "promptEngine", promptEngine);
        ReflectionTestUtils.setField(selfHealingAgent, "providerManager", selfHealingProviderManager);

        AgentRegistry registry = new AgentRegistry();
        registry.register(java.util.UUID.randomUUID(), agent);
        registry.register(java.util.UUID.randomUUID(), tcAgent);
        registry.register(java.util.UUID.randomUUID(), scriptsAgent);
        registry.register(java.util.UUID.randomUUID(), execAgent);
        registry.register(java.util.UUID.randomUUID(), bugAgent);
        registry.register(java.util.UUID.randomUUID(), reportingAgent);
        registry.register(java.util.UUID.randomUUID(), learningAgent);
        registry.register(java.util.UUID.randomUUID(), selfHealingAgent);

        AgentManagerImpl agentManager = new AgentManagerImpl();
        ReflectionTestUtils.setField(agentManager, "agentRegistry", registry);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.findAndRegisterModules();

        LLMResponseValidator responseValidator = new LLMResponseValidator();
        ReflectionTestUtils.setField(responseValidator, "objectMapper", mapper);

        PlaywrightExecutionEngine playwrightEngine = new PlaywrightExecutionEngine();
        ExecutionEngineFactory engineFactory = new ExecutionEngineFactory(List.of(playwrightEngine));

        qaAnalysisStep = new QAAnalysisStep();
        ReflectionTestUtils.setField(qaAnalysisStep, "agentManager", agentManager);
        ReflectionTestUtils.setField(qaAnalysisStep, "objectMapper", mapper);

        testCaseGenerationStep = new TestCaseGenerationStep();
        ReflectionTestUtils.setField(testCaseGenerationStep, "agentManager", agentManager);
        ReflectionTestUtils.setField(testCaseGenerationStep, "objectMapper", mapper);
        ReflectionTestUtils.setField(testCaseGenerationStep, "responseValidator", responseValidator);

        scriptGenerationStep = new ScriptGenerationStep();
        ReflectionTestUtils.setField(scriptGenerationStep, "agentManager", agentManager);
        ReflectionTestUtils.setField(scriptGenerationStep, "objectMapper", mapper);
        ReflectionTestUtils.setField(scriptGenerationStep, "responseValidator", responseValidator);

        executionStep = new ExecutionStep();
        ReflectionTestUtils.setField(executionStep, "agentManager", agentManager);
        ReflectionTestUtils.setField(executionStep, "objectMapper", mapper);
        ReflectionTestUtils.setField(executionStep, "engineFactory", engineFactory);

        bugAnalysisStep = new BugAnalysisStep();
        ReflectionTestUtils.setField(bugAnalysisStep, "agentManager", agentManager);
        ReflectionTestUtils.setField(bugAnalysisStep, "objectMapper", mapper);
        ReflectionTestUtils.setField(bugAnalysisStep, "responseValidator", responseValidator);

        reportingStep = new ReportingStep();
        ReflectionTestUtils.setField(reportingStep, "agentManager", agentManager);
        ReflectionTestUtils.setField(reportingStep, "objectMapper", mapper);
        ReflectionTestUtils.setField(reportingStep, "responseValidator", responseValidator);

        // Wire LearningStep with LearningEngineImpl
        com.aiqaos.learning.analyzer.FailurePatternAnalyzer learningAnalyzer = new com.aiqaos.learning.analyzer.FailurePatternAnalyzer();
        com.aiqaos.learning.healing.SelfHealingEngine learningHealing = new com.aiqaos.learning.healing.SelfHealingEngine();
        com.aiqaos.learning.memory.LearningMemoryStore learningStore = new com.aiqaos.learning.memory.LearningMemoryStore();
        
        // Use a simple stub memory store for the test
        MemoryStore simpleMemStore = new MemoryStore() {
            private final java.util.Map<String, Object> map = new java.util.HashMap<>();
            @Override public void put(String key, Object val, java.time.Duration ttl) { map.put(key, val); }
            @Override public java.util.Optional<Object> get(String key) { return java.util.Optional.ofNullable(map.get(key)); }
            @Override public void remove(String key) { map.remove(key); }
            @Override public void clear() { map.clear(); }
        };
        ReflectionTestUtils.setField(learningStore, "memoryStore", simpleMemStore);

        com.aiqaos.learning.engine.LearningEngineImpl learningEngine = new com.aiqaos.learning.engine.LearningEngineImpl();
        ReflectionTestUtils.setField(learningEngine, "agentManager", agentManager);
        ReflectionTestUtils.setField(learningEngine, "analyzer", learningAnalyzer);
        ReflectionTestUtils.setField(learningEngine, "healingEngine", learningHealing);
        ReflectionTestUtils.setField(learningEngine, "memoryStore", learningStore);
        ReflectionTestUtils.setField(learningEngine, "objectMapper", mapper);

        learningStep = new LearningStep();
        ReflectionTestUtils.setField(learningStep, "learningEngine", learningEngine);
        ReflectionTestUtils.setField(learningStep, "responseValidator", responseValidator);
        ReflectionTestUtils.setField(learningStep, "objectMapper", mapper);
        ReflectionTestUtils.setField(learningStep, "memoryStore", learningStore);

        // Setup SelfHealingStep wiring
        com.aiqaos.healing.memory.RecoveryHistoryStore recoveryStore = new com.aiqaos.healing.memory.RecoveryHistoryStore();
        ReflectionTestUtils.setField(recoveryStore, "memoryStore", simpleMemStore);

        com.aiqaos.healing.engine.SelfHealingEngineImpl activeHealingEngine = new com.aiqaos.healing.engine.SelfHealingEngineImpl();
        ReflectionTestUtils.setField(activeHealingEngine, "strategyResolver", new com.aiqaos.healing.strategy.RecoveryStrategyResolver());
        ReflectionTestUtils.setField(activeHealingEngine, "historyStore", recoveryStore);
        ReflectionTestUtils.setField(activeHealingEngine, "executionEngineFactory", engineFactory);
        ReflectionTestUtils.setField(activeHealingEngine, "scriptGenerationService", new com.aiqaos.healing.service.ScriptGenerationService() {
            @Override
            public GeneratedScriptSuite regenerate(com.aiqaos.core.model.QAExecutionReport r, String reason) {
                return new GeneratedScriptSuite();
            }
        });

        selfHealingStep = new SelfHealingStep();
        ReflectionTestUtils.setField(selfHealingStep, "agentManager", agentManager);
        ReflectionTestUtils.setField(selfHealingStep, "healingEngine", activeHealingEngine);
        ReflectionTestUtils.setField(selfHealingStep, "responseValidator", responseValidator);
        ReflectionTestUtils.setField(selfHealingStep, "objectMapper", mapper);

        orchestrator = new AutonomousQAPipelineOrchestrator(
                requirementReaderStep,
                qaAnalysisStep,
                testCaseGenerationStep,
                scriptGenerationStep,
                executionStep,
                bugAnalysisStep,
                reportingStep,
                learningStep,
                selfHealingStep
        );
    }

    @Test
    public void testSuccessfulPipelineExecution() {
        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("storyPath", "US-001.md");
        request.setInputs(inputs);

        WorkflowResponse response = orchestrator.runPipeline(request, context);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(WorkflowStatus.COMPLETED, context.getStatus());

        AutonomousQAWorkflowState state = context.getQaWorkflowState();
        assertNotNull(state);
        assertNotNull(state.getRequirementContext());
        assertEquals("Test Title", state.getRequirementContext().getTitle());

        assertNotNull(state.getQaAnalysisResult());
        assertTrue(state.getQaAnalysisResult().isReadyForTestDesign());

        assertNotNull(state.getGeneratedTestCaseSuite());
        assertEquals(2, state.getGeneratedTestCaseSuite().getTestCases().size());

        assertNotNull(state.getGeneratedScriptSuite());
        assertEquals(2, state.getGeneratedScriptSuite().getScripts().size());

        assertNotNull(state.getExecutionResult());
        assertTrue(state.getExecutionResult().isSuccess());

        // BugAnalysisStep bypasses on success and creates a CLOSED placeholder report
        assertNotNull(state.getBugAnalysisReport());

        // ReportingStep produces a QAExecutionReport
        assertNotNull(state.getQaExecutionReport());
        assertEquals("RPT-001", state.getQaExecutionReport().getReportId());
        assertEquals("COMPLETED", state.getQaExecutionReport().getStatus());
        assertEquals("PASS", state.getQaExecutionReport().getOverallResult());
        assertEquals("v1.0", state.getQaExecutionReport().getReportVersion());
        assertEquals("AI-QA-OS ReportingAgent", state.getQaExecutionReport().getGeneratedBy());

        // LearningStep produces a LearningResult
        assertNotNull(state.getLearningResult());
        assertNotNull(state.getLearningResult().getPatterns());
        assertNotNull(state.getLearningResult().getRecommendations());

        assertNotNull(state.getLearningFeedback());
        // SelfHealingStep produces a SelfHealingResult
        assertNotNull(state.getSelfHealingResult());
        assertFalse(state.getSelfHealingResult().isHealingApplied());
        assertEquals("BYPASSED_SUCCESS", state.getSelfHealingResult().getRecoveryStatus());

        assertEquals("PASS", context.getVariables().get("executionStatus"));
    }

    @Test
    public void testFailedExecutionWithBugAnalysisBranching() {
        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("storyPath", "US-001.md");
        inputs.put("simulateFailure", true);
        request.setInputs(inputs);

        WorkflowResponse response = orchestrator.runPipeline(request, context);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(WorkflowStatus.COMPLETED, context.getStatus());

        AutonomousQAWorkflowState state = context.getQaWorkflowState();
        assertNotNull(state);
        assertNotNull(state.getExecutionResult());

        assertNotNull(state.getBugAnalysisReport());
        assertNotNull(state.getBugAnalysisReport().getRootCause());
        assertNotNull(state.getBugAnalysisReport().getFailureCategory());

        // Since execution failed, SelfHealingStep executes retry.
        // Under test stub Playwright engine, retry returns SUCCESS (execute retry returns true)
        // Which sets executionStatus to PASS and selfHealingResult.isRetrySuccessful to true
        assertNotNull(state.getSelfHealingResult());
        assertTrue(state.getSelfHealingResult().isHealingApplied());
        assertTrue(state.getSelfHealingResult().isRetrySuccessful());

        assertEquals("PASS", context.getVariables().get("executionStatus"));

        // ReportingStep runs before SelfHealingStep in the approved pipeline order, so it
        // originally reports the failure. After a successful heal, SelfHealingStep promotes
        // the healed execution to be the execution-of-record and patches the existing report
        // in place so the FINAL report reflects recovery, not the initial failure.
        assertTrue(state.getExecutionResult().isSuccess());
        assertNotNull(state.getQaExecutionReport());
        assertEquals("PASS", state.getQaExecutionReport().getOverallResult());

        // The original failed execution remains preserved for audit purposes.
        assertNotNull(state.getSelfHealingResult().getOriginalExecution());
        assertFalse(state.getSelfHealingResult().getOriginalExecution().isSuccess());
    }

    @Test
    public void testPipelineRetryStrategy() {
        WorkflowRequest request = new WorkflowRequest();
        WorkflowContext context = new WorkflowContext();
        context.setQaWorkflowState(new AutonomousQAWorkflowState());
        context.setRetryCount(2);

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("storyPath", "US-001.md");
        request.setInputs(inputs);

        reader.setExceptionToThrow(new IOException("Transient File Read Error"));

        WorkflowResponse response = orchestrator.runPipeline(request, context);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(WorkflowStatus.COMPLETED, context.getStatus());
        assertEquals(2, reader.getCallCount());
    }

    private static class StubRequirementReader extends RequirementReader {
        private final String returnValue;
        private IOException exceptionToThrow;
        private int callCount = 0;

        public StubRequirementReader(String returnValue) {
            this.returnValue = returnValue;
        }

        public void setExceptionToThrow(IOException exceptionToThrow) {
            this.exceptionToThrow = exceptionToThrow;
        }

        public int getCallCount() {
            return callCount;
        }

        @Override
        public String readRequirement(String filePath) throws IOException {
            callCount++;
            if (exceptionToThrow != null) {
                IOException toThrow = exceptionToThrow;
                exceptionToThrow = null;
                throw toThrow;
            }
            return returnValue;
        }
    }

    private static class StubRequirementParser extends RequirementParser {
        private final RequirementContext returnValue;

        public StubRequirementParser(RequirementContext returnValue) {
            this.returnValue = returnValue;
        }

        @Override
        public RequirementContext parse(String rawContent) {
            return returnValue;
        }
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
