package com.aiqaos.workflow.pipeline;

import com.aiqaos.agent.registry.AgentRegistry;
import com.aiqaos.agent.manager.AgentManagerImpl;
import com.aiqaos.core.contract.AgentRequest;
import com.aiqaos.core.contract.AgentResponse;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.AutonomousQAWorkflowState;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.enums.AgentType;
import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.LearningResult;
import com.aiqaos.execution.engine.ExecutionEngine;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
import com.aiqaos.healing.engine.SelfHealingEngineImpl;
import com.aiqaos.healing.memory.RecoveryHistoryStore;
import com.aiqaos.healing.strategy.RecoveryStrategyResolver;
import com.aiqaos.memory.store.MemoryStore;
import com.aiqaos.workflow.validation.LLMResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SelfHealingStepIntegrationTest {

    private SelfHealingStep step;
    private AutonomousQAWorkflowState state;
    private WorkflowContext context;

    @BeforeEach
    void setUp() {
        step = new SelfHealingStep();
        state = new AutonomousQAWorkflowState();
        context = new WorkflowContext();
        context.setQaWorkflowState(state);
        context.setVariables(new HashMap<>());

        // Mock Agent
        AgentRegistry registry = new AgentRegistry();
        AgentManagerImpl agentManager = new AgentManagerImpl();
        ReflectionTestUtils.setField(agentManager, "agentRegistry", registry);

        agentManager.registerAgent(new com.aiqaos.core.engine.Agent<AgentRequest, AgentResponse>() {
            @Override public AgentResponse execute(AgentRequest request, com.aiqaos.core.context.AgentContext c) {
                AgentResponse res = new AgentResponse();
                res.setStatus("SUCCESS");
                res.setContent("{\"healingAction\":\"LOCATOR_UPDATE\",\"reason\":\"Test healing\",\"confidence\":0.9,\"retryRequired\":true,\"scriptRegenerationRequired\":false}");
                return res;
            }
            @Override public com.aiqaos.core.enums.AgentStatus getStatus() { return com.aiqaos.core.enums.AgentStatus.IDLE; }
            @Override public AgentType getType() { return AgentType.SELF_HEALING_ENGINEER; }
        });

        // Mock Healing Engine
        SelfHealingEngineImpl healingEngine = new SelfHealingEngineImpl();

        MemoryStore simpleMemStore = new MemoryStore() {
            private final java.util.Map<String, Object> map = new java.util.HashMap<>();
            @Override public void put(String key, Object val, java.time.Duration ttl) { map.put(key, val); }
            @Override public Optional<Object> get(String key) { return Optional.ofNullable(map.get(key)); }
            @Override public void remove(String key) { map.remove(key); }
            @Override public void clear() { map.clear(); }
        };

        RecoveryHistoryStore historyStore = new RecoveryHistoryStore();
        ReflectionTestUtils.setField(historyStore, "memoryStore", simpleMemStore);

        ExecutionEngineFactory executionEngineFactory = new ExecutionEngineFactory(java.util.List.of(new ExecutionEngine() {
            @Override public String getSupportedFramework() { return "Playwright"; }
            @Override public ExecutionResult execute(GeneratedScriptSuite s, com.aiqaos.execution.engine.ExecutionConfiguration c) {
                ExecutionResult res = new ExecutionResult();
                res.setSuccess(true);
                res.setStatus("PASS");
                res.setPassed(2);
                res.setFailed(0);
                return res;
            }
            @Override public void cancel() {}
            @Override public boolean isRunning() { return false; }
        }));

        ReflectionTestUtils.setField(healingEngine, "strategyResolver", new RecoveryStrategyResolver());
        ReflectionTestUtils.setField(healingEngine, "historyStore", historyStore);
        ReflectionTestUtils.setField(healingEngine, "executionEngineFactory", executionEngineFactory);
        ReflectionTestUtils.setField(healingEngine, "scriptGenerationService", new com.aiqaos.healing.service.ScriptGenerationService() {
            @Override
            public GeneratedScriptSuite regenerate(com.aiqaos.core.model.QAExecutionReport r, String reason) {
                return new GeneratedScriptSuite();
            }
        });

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.findAndRegisterModules();

        LLMResponseValidator validator = new LLMResponseValidator();
        ReflectionTestUtils.setField(validator, "objectMapper", mapper);

        ReflectionTestUtils.setField(step, "agentManager", agentManager);
        ReflectionTestUtils.setField(step, "healingEngine", healingEngine);
        ReflectionTestUtils.setField(step, "responseValidator", validator);
        ReflectionTestUtils.setField(step, "objectMapper", mapper);
    }

    @Test
    void testSelfHealingStepSuccessfulExecutionBypass() {
        ExecutionResult execPass = new ExecutionResult();
        execPass.setSuccess(true);
        state.setExecutionResult(execPass);

        WorkflowResponse response = step.execute(new WorkflowRequest(), context);

        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(state.getSelfHealingResult());
        assertFalse(state.getSelfHealingResult().isHealingApplied());
        assertEquals("BYPASSED_SUCCESS", state.getSelfHealingResult().getRecoveryStatus());
    }

    @Test
    void testSelfHealingStepFailedExecutionRetryPass() {
        ExecutionResult execFail = new ExecutionResult();
        execFail.setExecutionId("EXEC-INTEG");
        execFail.setSuccess(false);
        execFail.setErrorMessage("Selector mismatch error");
        execFail.setFailed(2);
        state.setExecutionResult(execFail);

        BugAnalysisReport bugReport = new BugAnalysisReport();
        bugReport.setFailureCategory("LOCATOR_FAIL");
        state.setBugAnalysisReport(bugReport);

        LearningResult learningResult = new LearningResult();
        state.setLearningResult(learningResult);

        // Report as originally compiled by ReportingStep, reflecting the initial failure
        com.aiqaos.core.model.QAExecutionReport initialReport = new com.aiqaos.core.model.QAExecutionReport();
        initialReport.setReportId("RPT-INTEG");
        initialReport.setOverallResult("FAIL");
        initialReport.setTotalTestCases(2);
        initialReport.setPassedTests(0);
        initialReport.setFailedTests(2);
        initialReport.setPassPercentage(0.0);
        initialReport.setSummary("Pipeline execution failed");
        initialReport.setExecutionResult(execFail);
        state.setQaExecutionReport(initialReport);

        WorkflowResponse response = step.execute(new WorkflowRequest(), context);

        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(state.getSelfHealingResult());
        assertTrue(state.getSelfHealingResult().isHealingApplied());
        assertTrue(state.getSelfHealingResult().isRetrySuccessful());
        assertEquals("PASS", context.getVariables().get("executionStatus"));

        // 1. Execution-of-record is promoted to the healed (passing) execution
        assertNotNull(state.getExecutionResult());
        assertTrue(state.getExecutionResult().isSuccess());

        // 2 & 3. The existing QAExecutionReport is patched, not replaced (reportId preserved)
        // and now reflects the healed outcome: overallResult, counts, and percentage.
        assertNotNull(state.getQaExecutionReport());
        assertEquals("RPT-INTEG", state.getQaExecutionReport().getReportId());
        assertEquals("PASS", state.getQaExecutionReport().getOverallResult());
        assertEquals(2, state.getQaExecutionReport().getTotalTestCases());
        assertEquals(2, state.getQaExecutionReport().getPassedTests());
        assertEquals(0, state.getQaExecutionReport().getFailedTests());
        assertEquals(100.0, state.getQaExecutionReport().getPassPercentage());
        assertTrue(state.getQaExecutionReport().getSummary().toLowerCase().contains("self-healing"));

        // 4. Original failed execution remains preserved for audit purposes
        assertNotNull(state.getSelfHealingResult().getOriginalExecution());
        assertFalse(state.getSelfHealingResult().getOriginalExecution().isSuccess());
        assertEquals("Selector mismatch error", state.getSelfHealingResult().getOriginalExecution().getErrorMessage());
    }
}
