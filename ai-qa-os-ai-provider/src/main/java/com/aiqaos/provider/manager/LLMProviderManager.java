package com.aiqaos.provider.manager;

import com.aiqaos.observability.metrics.LLMMetricsCollector;
import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.cost.BudgetVerdict;
import com.aiqaos.provider.cost.CostBudgetEnforcer;
import com.aiqaos.provider.cost.CostTracker;
import com.aiqaos.provider.exception.BudgetExceededException;
import com.aiqaos.provider.model.LLMRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.provider.claude.ClaudeProvider;
import com.aiqaos.provider.provider.gemini.GeminiProvider;
import com.aiqaos.provider.provider.openai.OpenAIProvider;
import com.aiqaos.provider.router.ModelRouter;
import com.aiqaos.provider.cache.LlmSemanticCacheManager;
import com.aiqaos.provider.provider.simulator.SimulatorProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LLMProviderManager {

    private static final Logger log = LoggerFactory.getLogger(LLMProviderManager.class);

    // ENT-3: optional LLM cost-quota enforcement (disabled by default; null in direct-construction tests).
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private CostBudgetEnforcer costBudgetEnforcer;

    private final OpenAIProvider        openAIProvider;
    private final ClaudeProvider        claudeProvider;
    private final GeminiProvider        geminiProvider;
    private final ModelRouter           modelRouter;
    private final LLMResilienceManager  resilienceManager;
    private final CostTracker           costTracker;
    private final LLMMetricsCollector   metricsCollector;
    private final ObjectProvider<LlmSemanticCacheManager> promptCacheManagerProvider;
    private final ObjectProvider<SimulatorProvider> simulatorProviderSupplier;

    public LLMProviderManager(OpenAIProvider openAIProvider,
                              ClaudeProvider claudeProvider,
                              GeminiProvider geminiProvider,
                              ModelRouter modelRouter,
                              LLMResilienceManager resilienceManager,
                              CostTracker costTracker,
                              LLMMetricsCollector metricsCollector) {
        this(openAIProvider, claudeProvider, geminiProvider, modelRouter, resilienceManager, costTracker, metricsCollector, null, null);
    }

    public LLMProviderManager(OpenAIProvider openAIProvider,
                              ClaudeProvider claudeProvider,
                              GeminiProvider geminiProvider,
                              ModelRouter modelRouter,
                              LLMResilienceManager resilienceManager,
                              CostTracker costTracker,
                              LLMMetricsCollector metricsCollector,
                              ObjectProvider<LlmSemanticCacheManager> promptCacheManagerProvider) {
        this(openAIProvider, claudeProvider, geminiProvider, modelRouter, resilienceManager, costTracker, metricsCollector, promptCacheManagerProvider, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public LLMProviderManager(OpenAIProvider openAIProvider,
                              ClaudeProvider claudeProvider,
                              GeminiProvider geminiProvider,
                              ModelRouter modelRouter,
                              LLMResilienceManager resilienceManager,
                              CostTracker costTracker,
                              LLMMetricsCollector metricsCollector,
                              ObjectProvider<LlmSemanticCacheManager> promptCacheManagerProvider,
                              ObjectProvider<SimulatorProvider> simulatorProviderSupplier) {
        this.openAIProvider = openAIProvider;
        this.claudeProvider = claudeProvider;
        this.geminiProvider = geminiProvider;
        this.modelRouter = modelRouter;
        this.resilienceManager = resilienceManager;
        this.costTracker = costTracker;
        this.metricsCollector = metricsCollector;
        this.promptCacheManagerProvider = promptCacheManagerProvider;
        this.simulatorProviderSupplier = simulatorProviderSupplier;
    }

    public LLMResponse generate(LLMRequest request) {
        LlmSemanticCacheManager cacheManager = promptCacheManagerProvider != null ? promptCacheManagerProvider.getIfAvailable() : null;
        if (cacheManager != null) {
            Optional<LLMResponse> cached = cacheManager.findCachedResponse(request);
            if (cached.isPresent()) {
                LLMResponse cachedResponse = cached.get();
                metricsCollector.recordLLMCall(cachedResponse.getModel(), 0, cachedResponse.getLatencyMs());
                return cachedResponse;
            }
        }

        // DX-3: If model explicitly specifies simulator/mock or prompt is forced local, direct to SimulatorProvider
        if (request != null && request.getModel() != null &&
                (request.getModel().toLowerCase().contains("simulator") || request.getModel().toLowerCase().contains("mock"))) {
            SimulatorProvider sim = simulatorProviderSupplier != null ? simulatorProviderSupplier.getIfAvailable() : null;
            if (sim != null) {
                return sim.generate(request);
            }
        }

        // ENT-3: pre-flight cost quota (soft cap). No-op when disabled; a cache/simulator hit above never reaches here.
        if (costBudgetEnforcer != null) {
            BudgetVerdict verdict = costBudgetEnforcer.check(request);
            if (!verdict.isAllowed()) {
                String message = "LLM cost budget exceeded [" + verdict.getScope()
                        + ": limit " + verdict.getLimit() + ", spent " + verdict.getSpend() + "]";
                if (costBudgetEnforcer.isEnforce()) {
                    throw new BudgetExceededException(message);
                }
                log.warn("[cost-quota] {} (warn mode — allowing)", message);
            }
        }

        String targetProviderName = modelRouter.routeModel(request.getPurpose());

        LLMProvider primary = selectProvider(targetProviderName);
        LLMProvider fallback = selectFallback(primary);

        LLMResponse response = resilienceManager.executeWithFallback(primary, fallback, request);

        // Record cost tracking & metrics observability metrics
        costTracker.track(request, response, primary.getProviderName());
        metricsCollector.recordLLMCall(response.getModel(), response.getUsage().getInputTokens() + response.getUsage().getOutputTokens(), response.getLatencyMs());

        // Cache response for future semantically identical requests
        if (cacheManager != null) {
            cacheManager.cacheResponse(request, response);
        }

        return response;
    }

    private LLMProvider selectProvider(String name) {
        if ("Simulator".equalsIgnoreCase(name) || "Local-Simulator".equalsIgnoreCase(name)) {
            SimulatorProvider sim = simulatorProviderSupplier != null ? simulatorProviderSupplier.getIfAvailable() : null;
            if (sim != null) return sim;
        }

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
        SimulatorProvider sim = simulatorProviderSupplier != null ? simulatorProviderSupplier.getIfAvailable() : null;
        if (sim != null && primary != sim) {
            return sim;
        }

        for (LLMProvider candidate : List.of(geminiProvider, openAIProvider, claudeProvider)) {
            if (candidate != primary && candidate.isAvailable()) {
                return candidate;
            }
        }
        return primary;
    }
}