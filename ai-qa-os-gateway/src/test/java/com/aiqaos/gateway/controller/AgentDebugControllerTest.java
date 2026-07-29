package com.aiqaos.gateway.controller;

import com.aiqaos.gateway.debug.AgentDebuggerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AgentDebugControllerTest {

    private AgentDebugController controller;
    private AgentDebuggerService service;

    @BeforeEach
    void setUp() {
        service = new AgentDebuggerService(
                new TestingObjectProvider<com.aiqaos.provider.manager.LLMProviderManager>(null),
                new TestingObjectProvider<com.aiqaos.provider.provider.simulator.SimulatorProvider>(null)
        );
        controller = new AgentDebugController(service);
    }

    @Test
    @DisplayName("DX-4: POST /api/debug/playground returns playground result")
    void testPlaygroundEndpoint() {
        AgentDebuggerService.PlaygroundRequest req = new AgentDebuggerService.PlaygroundRequest();
        req.setPrompt("Generate Playwright script for login button click");

        ResponseEntity<AgentDebuggerService.PlaygroundResult> response = controller.testPlaygroundPrompt(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getOutputText());
    }

    @Test
    @DisplayName("DX-4: GET & POST /api/debug/traces endpoints handle traces")
    void testTracesEndpoints() {
        String execId = "exec-ctrl-202";
        AgentDebuggerService.StepReasoningTrace trace = new AgentDebuggerService.StepReasoningTrace(
                "QAAnalysisStep", "QAAnalystAgent", "Risk analysis prompt",
                "Risk Score: 0.2", "simulator", 15L, 80, 0.98
        );

        ResponseEntity<Void> postResp = controller.recordStepTrace(execId, trace);
        assertEquals(HttpStatus.OK, postResp.getStatusCode());

        ResponseEntity<List<AgentDebuggerService.StepReasoningTrace>> getResp = controller.getExecutionTraces(execId);
        assertEquals(HttpStatus.OK, getResp.getStatusCode());
        assertNotNull(getResp.getBody());
        assertEquals(1, getResp.getBody().size());
        assertEquals("QAAnalysisStep", getResp.getBody().get(0).getStepName());
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
