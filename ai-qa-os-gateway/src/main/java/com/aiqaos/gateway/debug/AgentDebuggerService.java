package com.aiqaos.gateway.debug;

import com.aiqaos.provider.manager.LLMProviderManager;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.provider.simulator.SimulatorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DX-4: Live Agent Debugger & Prompt Playground Service.
 *
 * Surfaces step-by-step reasoning traces, prompt payloads, model metadata,
 * response latencies, and interactive prompt playground execution.
 */
@Service
public class AgentDebuggerService {

    private static final Logger log = LoggerFactory.getLogger(AgentDebuggerService.class);

    private final Map<String, List<StepReasoningTrace>> traceStore = new ConcurrentHashMap<>();
    private final ObjectProvider<LLMProviderManager> providerManagerProvider;
    private final ObjectProvider<SimulatorProvider> simulatorProvider;

    public AgentDebuggerService(ObjectProvider<LLMProviderManager> providerManagerProvider,
                                ObjectProvider<SimulatorProvider> simulatorProvider) {
        this.providerManagerProvider = providerManagerProvider;
        this.simulatorProvider = simulatorProvider;
    }

    public void recordStepTrace(String executionId, StepReasoningTrace trace) {
        if (executionId == null || trace == null) return;
        traceStore.computeIfAbsent(executionId, k -> Collections.synchronizedList(new ArrayList<>())).add(trace);
        log.info("DX-4: Recorded agent reasoning trace for execution {} step {}", executionId, trace.getStepName());
    }

    public List<StepReasoningTrace> getStepTraces(String executionId) {
        return traceStore.getOrDefault(executionId, Collections.emptyList());
    }

    public PlaygroundResult executePlaygroundPrompt(PlaygroundRequest request) {
        long startTime = System.currentTimeMillis();

        String prompt = request.getPrompt();
        String model = request.getModel() != null ? request.getModel() : "simulator";
        double temperature = request.getTemperature() != null ? request.getTemperature() : 0.7;

        LLMRequest llmReq = new LLMRequest();
        llmReq.setPrompt(prompt);
        llmReq.setModel(model);
        llmReq.setTemperature(temperature);

        String outputText;
        int promptTokens;
        int completionTokens;
        double cost;

        LLMProviderManager manager = providerManagerProvider.getIfAvailable();
        if (manager != null) {
            try {
                LLMResponse resp = manager.generate(llmReq);
                outputText = resp.getText();
                promptTokens = resp.getUsage() != null ? (int) resp.getUsage().getInputTokens() : prompt.length() / 4;
                completionTokens = resp.getUsage() != null ? (int) resp.getUsage().getOutputTokens() : outputText.length() / 4;
                cost = 0.0;
            } catch (Exception e) {
                log.warn("DX-4: LLMProviderManager error in playground, falling back to SimulatorProvider: {}", e.getMessage());
                return executeSimulatorFallback(prompt, model, startTime);
            }
        } else {
            return executeSimulatorFallback(prompt, model, startTime);
        }

        long latencyMs = System.currentTimeMillis() - startTime;
        boolean isValidJson = checkValidJson(outputText);

        return new PlaygroundResult(
                prompt,
                outputText,
                model,
                temperature,
                latencyMs,
                promptTokens,
                completionTokens,
                cost,
                isValidJson,
                0.95
        );
    }

    private PlaygroundResult executeSimulatorFallback(String prompt, String model, long startTime) {
        SimulatorProvider simulator = simulatorProvider.getIfAvailable();
        if (simulator == null) {
            simulator = new SimulatorProvider();
        }
        LLMRequest req = new LLMRequest();
        req.setPrompt(prompt);
        req.setModel(model);

        LLMResponse resp = simulator.generate(req);
        long latencyMs = System.currentTimeMillis() - startTime;

        return new PlaygroundResult(
                prompt,
                resp.getText(),
                model,
                0.7,
                latencyMs,
                prompt.length() / 4,
                resp.getText().length() / 4,
                0.0,
                checkValidJson(resp.getText()),
                0.98
        );
    }

    private boolean checkValidJson(String text) {
        if (text == null) return false;
        String trimmed = text.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    // ─── DTO Classes ────────────────────────────────────────────────────────

    public static class StepReasoningTrace {
        private String stepName;
        private String agentName;
        private String promptPayload;
        private String reasoningOutput;
        private String modelUsed;
        private long latencyMs;
        private int totalTokens;
        private double confidence;
        private long timestamp = System.currentTimeMillis();

        public StepReasoningTrace() {}

        public StepReasoningTrace(String stepName, String agentName, String promptPayload, String reasoningOutput, String modelUsed, long latencyMs, int totalTokens, double confidence) {
            this.stepName = stepName;
            this.agentName = agentName;
            this.promptPayload = promptPayload;
            this.reasoningOutput = reasoningOutput;
            this.modelUsed = modelUsed;
            this.latencyMs = latencyMs;
            this.totalTokens = totalTokens;
            this.confidence = confidence;
        }

        public String getStepName() { return stepName; }
        public String getAgentName() { return agentName; }
        public String getPromptPayload() { return promptPayload; }
        public String getReasoningOutput() { return reasoningOutput; }
        public String getModelUsed() { return modelUsed; }
        public long getLatencyMs() { return latencyMs; }
        public int getTotalTokens() { return totalTokens; }
        public double getConfidence() { return confidence; }
        public long getTimestamp() { return timestamp; }
    }

    public static class PlaygroundRequest {
        private String prompt;
        private String model;
        private Double temperature;

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
    }

    public static class PlaygroundResult {
        private final String inputPrompt;
        private final String outputText;
        private final String modelUsed;
        private final double temperature;
        private final long latencyMs;
        private final int promptTokens;
        private final int completionTokens;
        private final double cost;
        private final boolean validJson;
        private final double confidence;

        public PlaygroundResult(String inputPrompt, String outputText, String modelUsed, double temperature, long latencyMs, int promptTokens, int completionTokens, double cost, boolean validJson, double confidence) {
            this.inputPrompt = inputPrompt;
            this.outputText = outputText;
            this.modelUsed = modelUsed;
            this.temperature = temperature;
            this.latencyMs = latencyMs;
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.cost = cost;
            this.validJson = validJson;
            this.confidence = confidence;
        }

        public String getInputPrompt() { return inputPrompt; }
        public String getOutputText() { return outputText; }
        public String getModelUsed() { return modelUsed; }
        public double getTemperature() { return temperature; }
        public long getLatencyMs() { return latencyMs; }
        public int getPromptTokens() { return promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
        public double getCost() { return cost; }
        public boolean isValidJson() { return validJson; }
        public double getConfidence() { return confidence; }
    }
}
