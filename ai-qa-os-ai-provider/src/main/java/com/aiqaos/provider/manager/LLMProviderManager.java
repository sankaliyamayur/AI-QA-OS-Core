package com.aiqaos.provider.manager;

import com.aiqaos.observability.metrics.LLMMetricsCollector;
import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.cost.CostTracker;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.provider.claude.ClaudeProvider;
import com.aiqaos.provider.provider.gemini.GeminiProvider;
import com.aiqaos.provider.provider.openai.OpenAIProvider;
import com.aiqaos.provider.router.ModelRouter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LLMProviderManager {

    private final OpenAIProvider        openAIProvider;
    private final ClaudeProvider        claudeProvider;
    private final GeminiProvider        geminiProvider;
    private final ModelRouter           modelRouter;
    private final LLMResilienceManager  resilienceManager;
    private final CostTracker           costTracker;
    private final LLMMetricsCollector   metricsCollector;

    public LLMProviderManager(OpenAIProvider openAIProvider,
                              ClaudeProvider claudeProvider,
                              GeminiProvider geminiProvider,
                              ModelRouter modelRouter,
                              LLMResilienceManager resilienceManager,
                              CostTracker costTracker,
                              LLMMetricsCollector metricsCollector) {
        this.openAIProvider = openAIProvider;
        this.claudeProvider = claudeProvider;
        this.geminiProvider = geminiProvider;
        this.modelRouter = modelRouter;
        this.resilienceManager = resilienceManager;
        this.costTracker = costTracker;
        this.metricsCollector = metricsCollector;
    }

    public LLMResponse generate(LLMRequest request) {
        String targetProviderName = modelRouter.routeModel(request.getPurpose());
        
        LLMProvider primary = selectProvider(targetProviderName);
        LLMProvider fallback = selectFallback(primary);

        LLMResponse response = resilienceManager.executeWithFallback(primary, fallback, request);

        // Record cost tracking & metrics observability metrics
        costTracker.track(request, response, primary.getProviderName());
        metricsCollector.recordLLMCall(response.getModel(), response.getUsage().getInputTokens() + response.getUsage().getOutputTokens(), response.getLatencyMs());

        return response;
    }

    private LLMProvider selectProvider(String name) {
        switch (name) {
            case "OpenAI" -> { return openAIProvider; }
            case "Claude" -> { return claudeProvider; }
            default       -> { return geminiProvider; }
        }
    }

    /**
     * Picks a configured provider other than the primary. Falling back to the same
     * provider re-runs the identical call with the identical credentials, which cannot
     * recover from the failure that triggered the fallback in the first place.
     */
    private LLMProvider selectFallback(LLMProvider primary) {
        for (LLMProvider candidate : List.of(geminiProvider, openAIProvider, claudeProvider)) {
            if (candidate != primary && candidate.isAvailable()) {
                return candidate;
            }
        }
        return primary;
    }
}