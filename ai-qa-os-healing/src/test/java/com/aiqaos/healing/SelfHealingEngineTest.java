package com.aiqaos.healing;

import com.aiqaos.core.enums.HealingActionType;
import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.LearningResult;
import com.aiqaos.core.model.SelfHealingRecommendation;
import com.aiqaos.core.model.SelfHealingResult;
import com.aiqaos.execution.engine.ExecutionEngine;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
import com.aiqaos.healing.engine.SelfHealingEngineImpl;
import com.aiqaos.healing.memory.RecoveryHistoryStore;
import com.aiqaos.healing.service.ScriptGenerationService;
import com.aiqaos.healing.strategy.RecoveryStrategyResolver;
import com.aiqaos.memory.store.MemoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SelfHealingEngineTest {

    private SelfHealingEngineImpl engine;
    private RecoveryHistoryStore historyStore;
    private ExecutionEngineFactory executionEngineFactory;
    private ScriptGenerationService scriptGenerationService;

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

        executionEngineFactory = new ExecutionEngineFactory(List.of(new ExecutionEngine() {
            @Override public String getSupportedFramework() { return "Playwright"; }
            @Override public ExecutionResult execute(com.aiqaos.core.model.GeneratedScriptSuite s, com.aiqaos.execution.engine.ExecutionConfiguration c) {
                ExecutionResult res = new ExecutionResult();
                res.setSuccess(true);
                res.setStatus("PASS");
                return res;
            }
            @Override public void cancel() {}
            @Override public boolean isRunning() { return false; }
        }));

        scriptGenerationService = (report, reason) -> new GeneratedScriptSuite();

        ReflectionTestUtils.setField(engine, "strategyResolver", new RecoveryStrategyResolver());
        ReflectionTestUtils.setField(engine, "historyStore", historyStore);
        ReflectionTestUtils.setField(engine, "executionEngineFactory", executionEngineFactory);
        ReflectionTestUtils.setField(engine, "scriptGenerationService", scriptGenerationService);
    }

    @Test
    void testHealWithLocatorUpdateReturnsSuccess() {
        ExecutionResult execResult = new ExecutionResult();
        execResult.setExecutionId("EXEC-001");
        execResult.setSuccess(false);
        execResult.setErrorMessage("Selector `#submit` not found");

        BugAnalysisReport bugReport = new BugAnalysisReport();
        bugReport.setFailureCategory("ELEMENT_NOT_FOUND");

        LearningResult learningResult = new LearningResult();
        SelfHealingRecommendation recommendation = new SelfHealingRecommendation();
        recommendation.setActionType(HealingActionType.LOCATOR_UPDATE);
        recommendation.setSuggestedAction("Use relative XPath resolver");
        learningResult.setRecommendations(Collections.singletonList(recommendation));

        SelfHealingResult result = engine.heal(execResult, learningResult, bugReport);

        assertNotNull(result);
        assertTrue(result.isHealingApplied());
        assertTrue(result.isRetrySuccessful());
        assertEquals("SUCCESS", result.getRecoveryStatus());
        assertEquals("LocatorRecoveryStrategy", result.getHealingStrategy());
        assertEquals(1, result.getRetryCount());
    }

    @Test
    void testHealWithScriptRegenerationReturnsSuccess() {
        ExecutionResult execResult = new ExecutionResult();
        execResult.setExecutionId("EXEC-002");
        execResult.setSuccess(false);
        execResult.setErrorMessage("Script syntax invalid");

        BugAnalysisReport bugReport = new BugAnalysisReport();
        bugReport.setFailureCategory("SCRIPT_ERROR");

        LearningResult learningResult = new LearningResult();
        SelfHealingRecommendation recommendation = new SelfHealingRecommendation();
        recommendation.setActionType(HealingActionType.SCRIPT_REGENERATE);
        recommendation.setSuggestedAction("Regenerate wait structures");
        learningResult.setRecommendations(Collections.singletonList(recommendation));

        SelfHealingResult result = engine.heal(execResult, learningResult, bugReport);

        assertNotNull(result);
        assertTrue(result.isHealingApplied());
        assertTrue(result.isRetrySuccessful());
        assertEquals("ScriptRegenerationStrategy", result.getHealingStrategy());
    }
}
