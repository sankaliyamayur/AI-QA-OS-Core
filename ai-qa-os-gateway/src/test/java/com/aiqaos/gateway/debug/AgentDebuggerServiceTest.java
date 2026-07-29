package com.aiqaos.gateway.debug;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AgentDebuggerServiceTest {

    private AgentDebuggerService debuggerService;

    @BeforeEach
    void setUp() {
        debuggerService = new AgentDebuggerService(
                new TestingObjectProvider<com.aiqaos.provider.manager.LLMProviderManager>(null),
                new TestingObjectProvider<com.aiqaos.provider.provider.simulator.SimulatorProvider>(null)
        );
    }

    @Test
    @DisplayName("DX-4: Should record and retrieve step reasoning traces by executionId")
    void testRecordAndGetTraces() {
        String execId = "exec-trace-101";
        AgentDebuggerService.StepReasoningTrace trace1 = new AgentDebuggerService.StepReasoningTrace(
                "StepRequirementReader", "RequirementReaderAgent", "Raw PRD prompt",
                "Extracted scenarios: Login, Checkout", "local-simulator", 12L, 120, 0.96
        );

        debuggerService.recordStepTrace(execId, trace1);

        List<AgentDebuggerService.StepReasoningTrace> traces = debuggerService.getStepTraces(execId);
        assertNotNull(traces);
        assertEquals(1, traces.size());
        assertEquals("StepRequirementReader", traces.get(0).getStepName());
        assertEquals(0.96, traces.get(0).getConfidence());
    }

    @Test
    @DisplayName("DX-4: Should execute prompt playground via simulator fallback and evaluate output")
    void testExecutePlaygroundPrompt() {
        AgentDebuggerService.PlaygroundRequest req = new AgentDebuggerService.PlaygroundRequest();
        req.setPrompt("Extract target locators for login screen");
        req.setModel("local-simulator");

        AgentDebuggerService.PlaygroundResult result = debuggerService.executePlaygroundPrompt(req);
        assertNotNull(result);
        assertNotNull(result.getOutputText());
        assertEquals("local-simulator", result.getModelUsed());
        assertTrue(result.isValidJson());
        assertTrue(result.getConfidence() > 0.9);
    }

    private static class TestingObjectProvider<T> implements ObjectProvider<T> {
        private final T instance;
        TestingObjectProvider(T instance) { this.instance = instance; }
        @Override public T getObject() { return instance; }
        @Override public T getObject(Object... args) { return instance; }
        @Override public T getIfAvailable() { return instance; }
        @Override public T getIfUnique() { return instance; }
    }
}
