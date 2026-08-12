package com.aiqaos.provider.manager;

import com.aiqaos.observability.metrics.LLMMetricsCollector;
import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.cost.BudgetVerdict;
import com.aiqaos.provider.cost.CostBudgetEnforcer;
import com.aiqaos.provider.cost.CostTracker;
import com.aiqaos.provider.cost.TokenBudgetEnforcer;
import com.aiqaos.provider.config.ProviderChainProperties;
import com.aiqaos.provider.exception.AllProvidersExhaustedException;
import com.aiqaos.provider.exception.BudgetExceededException;
import com.aiqaos.provider.exception.ProviderException;
import com.aiqaos.provider.exception.TokenBudgetExceededException;
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

    // AI-6: optional LLM token/context-budget enforcement (disabled by default; null in direct-construction tests).
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private TokenBudgetEnforcer tokenBudgetEnforcer;

    private final OpenAIProvider        openAIProvider;
    private final ClaudeProvider        claudeProvider;
    private final GeminiProvider        geminiProvider;
    private final ModelRouter           modelRouter;
    private final LLMResilienceManager  resilienceManager;
    private final CostTracker           costTracker;
    private final LLMMetricsCollector   metricsCollector;
    private final ObjectProvider<LlmSemanticCacheManager> promptCacheManagerProvider;
    private final ObjectProvider<SimulatorProvider> simulatorProviderSupplier;

    // Failover chain + mode. Optional so the direct-construction test constructors keep working;
    // a null here means the defaults apply, which are REAL mode and openai,claude,gemini,ollama.
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ProviderChainProperties chainProperties;

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

        // AI-6: pre-flight token/context-budget (soft cap). No-op when disabled; mirrors the cost check.
        if (tokenBudgetEnforcer != null) {
            BudgetVerdict verdict = tokenBudgetEnforcer.check(request);
            if (!verdict.isAllowed()) {
                String message = "LLM token/context budget exceeded [" + verdict.getScope()
                        + ": limit " + verdict.getLimit() + ", used " + verdict.getSpend() + "]";
                if (tokenBudgetEnforcer.isEnforce()) {
                    throw new TokenBudgetExceededException(message);
                }
                log.warn("[token-budget] {} (warn mode — allowing)", message);
            }
        }

        // The failover chain, in configured priority order. In REAL mode the Simulator is not in it
        // at all, so no provider failure can reach it (see ModelRouter.routeChain).
        ProviderChainProperties props = chainProperties != null ? chainProperties : new ProviderChainProperties();
        ModelRouter.Mode mode = props.resolvedMode() == ProviderChainProperties.Mode.SIMULATED
                ? ModelRouter.Mode.SIMULATED
                : ModelRouter.Mode.REAL;

        List<LLMProvider> chain = modelRouter.routeChain(request.getPurpose(), props.chainOrder(), mode);

        if (chain.isEmpty()) {
            // Nothing available: no key on any provider, Ollama disabled, Simulator excluded.
            // Fail loudly rather than silently degrade — that is the entire point of this path.
            throw new AllProvidersExhaustedException(List.of());
        }

        LLMResilienceManager.ChainResult result =
                resilienceManager.executeChain(chain, request, props.isRetryOnRateLimit(), props.getRetryBackoffMillis());

        LLMResponse response = result.response();
        LLMProvider serving = result.servingProvider();

        // Last line of defence: in REAL mode a Simulator response means the chain was mis-built.
        // Rejecting here turns a wiring regression into a failure instead of a false green run.
        if (mode == ModelRouter.Mode.REAL && ModelRouter.isSimulator(serving)) {
            throw new ProviderException(
                    "Simulator served a REAL-mode request — refusing to return a simulated result. "
                            + "This indicates a provider-chain wiring fault.", null, 0, false);
        }

        // Attribute the response to the provider that ACTUALLY served it, not the one first chosen.
        // Recording the intended provider is how agent_traces ended up with rows claiming
        // provider='Gemini' against model='local-simulator-v1'.
        costTracker.track(request, response, serving.getProviderName());
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