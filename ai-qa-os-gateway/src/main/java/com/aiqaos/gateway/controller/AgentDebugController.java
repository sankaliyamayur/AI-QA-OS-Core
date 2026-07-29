package com.aiqaos.gateway.controller;

import com.aiqaos.gateway.debug.AgentDebuggerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DX-4: Live Agent Debugger & Prompt Playground REST Controller.
 */
@RestController
@RequestMapping("/api/debug")
public class AgentDebugController {

    private final AgentDebuggerService debuggerService;

    public AgentDebugController(AgentDebuggerService debuggerService) {
        this.debuggerService = debuggerService;
    }

    @PostMapping("/playground")
    public ResponseEntity<AgentDebuggerService.PlaygroundResult> testPlaygroundPrompt(
            @RequestBody AgentDebuggerService.PlaygroundRequest request) {
        if (request == null || request.getPrompt() == null || request.getPrompt().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        AgentDebuggerService.PlaygroundResult result = debuggerService.executePlaygroundPrompt(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/traces/{executionId}")
    public ResponseEntity<List<AgentDebuggerService.StepReasoningTrace>> getExecutionTraces(
            @PathVariable String executionId) {
        List<AgentDebuggerService.StepReasoningTrace> traces = debuggerService.getStepTraces(executionId);
        return ResponseEntity.ok(traces);
    }

    @PostMapping("/traces/{executionId}")
    public ResponseEntity<Void> recordStepTrace(
            @PathVariable String executionId,
            @RequestBody AgentDebuggerService.StepReasoningTrace trace) {
        debuggerService.recordStepTrace(executionId, trace);
        return ResponseEntity.ok().build();
    }
}
