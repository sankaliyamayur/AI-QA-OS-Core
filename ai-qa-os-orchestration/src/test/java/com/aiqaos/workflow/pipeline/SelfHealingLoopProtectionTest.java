package com.aiqaos.workflow.pipeline;

import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.LearningResult;
import com.aiqaos.core.model.RecoveryAttempt;
import com.aiqaos.core.model.SelfHealingResult;
import com.aiqaos.execution.engine.ExecutionEngine;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
import com.aiqaos.healing.engine.SelfHealingEngineImpl;
import com.aiqaos.healing.memory.RecoveryHistoryStore;
import com.aiqaos.healing.strategy.RecoveryStrategyResolver;
import com.aiqaos.memory.store.MemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SelfHealingLoopProtectionTest {

    private SelfHealingEngineImpl engine;
    private RecoveryHistoryStore historyStore;

    @BeforeEach
    void setUp() {
        engine = new SelfHealingEngineImpl();

        MemoryStore simpleMemStore = new MemoryStore() {
            private final java.util.Map<String, Object> map = new java.util.HashMap<>();
            @Override public void put(String key, Object val, java.time.Duration ttl) { map.put(key, val); }
            @Override public Optional<Object> get(String key) { return Optional.ofNullable(map.get(key)); }
            @Override public void remove(String key) { map.remove(key); }
            @Override public void clear() { map.clear(); }
        };

        historyStore = new RecoveryHistoryStore();
        ReflectionTestUtils.setField(historyStore, "memoryStore", simpleMemStore);

        ExecutionEngineFactory executionEngineFactory = new ExecutionEngineFactory(java.util.List.of(new ExecutionEngine() {
            @Override public String getSupportedFramework() { return "Playwright"; }
            @Override public ExecutionResult execute(com.aiqaos.core.model.GeneratedScriptSuite s, com.aiqaos.execution.engine.ExecutionConfiguration c) {
                ExecutionResult res = new ExecutionResult();
                res.setSuccess(false);
                res.setErrorMessage("Still failing");
                return res;
            }
            @Override public void cancel() {}
            @Override public boolean isRunning() { return false; }
        }));

        ReflectionTestUtils.setField(engine, "strategyResolver", new RecoveryStrategyResolver());
        ReflectionTestUtils.setField(engine, "historyStore", historyStore);
        ReflectionTestUtils.setField(engine, "executionEngineFactory", executionEngineFactory);
        ReflectionTestUtils.setField(engine, "observabilityEventPublisher",
                NoOpObservabilityEventPublisherFactory.create());
    }

    @Test
    void testLoopProtectionTerminatesOnFourthAttempt() {
        ExecutionResult execResult = new ExecutionResult();
        execResult.setExecutionId("EXEC-LOOP");
        execResult.setSuccess(false);
        execResult.setErrorMessage("Selector mismatch error");

        BugAnalysisReport bugReport = new BugAnalysisReport();
        bugReport.setFailureCategory("LOCATOR_FAIL");

        LearningResult learningResult = new LearningResult();

        // Execution attempt 1
        SelfHealingResult res1 = engine.heal(execResult, learningResult, bugReport);
        assertTrue(res1.isHealingApplied());
        assertFalse(res1.isRetrySuccessful());
        assertEquals(1, res1.getRetryCount());

        // Execution attempt 2
        SelfHealingResult res2 = engine.heal(execResult, learningResult, bugReport);
        assertTrue(res2.isHealingApplied());
        assertEquals(2, res2.getRetryCount());

        // Execution attempt 3
        SelfHealingResult res3 = engine.heal(execResult, learningResult, bugReport);
        assertTrue(res3.isHealingApplied());
        assertEquals(3, res3.getRetryCount());

        // Execution attempt 4 - Loop Protection should trigger
        SelfHealingResult res4 = engine.heal(execResult, learningResult, bugReport);
        assertFalse(res4.isHealingApplied());
        assertFalse(res4.isRetrySuccessful());
        assertEquals("MAX_ATTEMPTS_EXCEEDED", res4.getRecoveryStatus());
        assertEquals(4, res4.getRetryCount());
    }
}
