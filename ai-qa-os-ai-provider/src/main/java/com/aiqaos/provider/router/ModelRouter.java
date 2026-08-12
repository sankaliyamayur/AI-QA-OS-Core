package com.aiqaos.provider.router;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.ProviderCapability;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ModelRouter {

    private final List<LLMProvider> providers;

    public ModelRouter(List<LLMProvider> providers) {
        this.providers = providers;
    }

    public String routeModel(String purpose) {
        ProviderCapability requiredCapability = resolveCapability(purpose);

        // Find first available provider that supports the capability
        return providers.stream()
            .filter(LLMProvider::isAvailable)
            .filter(p -> p.supports(requiredCapability))
            .map(LLMProvider::getProviderName)
            .findFirst()
            .orElse("Gemini"); // Default fallback
    }

    /**
     * The ordered failover chain for a purpose: every provider that is available and supports the
     * capability, in the configured priority order.
     *
     * <p>In {@link Mode#REAL} the Simulator is filtered out <b>unconditionally</b>. That exclusion is
     * the structural guarantee behind the whole failover change — a real run cannot fall back to a
     * canned answer, because the Simulator is not in the list to fall back to. Previously it was the
     * preferred fallback for every provider, which is how a rate-limited call became a green run.
     *
     * <p>Providers named in the order but absent from the context are skipped silently; unnamed
     * providers are appended after the named ones so a new provider is reachable before anyone
     * remembers to add it to the config.
     */
    public List<LLMProvider> routeChain(String purpose, List<String> priorityOrder, Mode mode) {
        ProviderCapability requiredCapability = resolveCapability(purpose);

        List<LLMProvider> eligible = providers.stream()
                .filter(LLMProvider::isAvailable)
                .filter(p -> p.supports(requiredCapability))
                .filter(p -> mode != Mode.REAL || !isSimulator(p))
                .collect(java.util.stream.Collectors.toList());

        List<String> order = priorityOrder == null ? List.of() : priorityOrder;
        List<LLMProvider> chain = new ArrayList<>();

        for (String name : order) {
            for (LLMProvider p : eligible) {
                if (p.getProviderName().equalsIgnoreCase(name) && !chain.contains(p)) {
                    chain.add(p);
                }
            }
        }
        for (LLMProvider p : eligible) {
            if (!chain.contains(p)) {
                chain.add(p);
            }
        }
        return chain;
    }

    /**
     * Matched on name rather than {@code instanceof} so the ai-provider module does not have to
     * depend on the simulator implementation, and so any future stand-in named "simulator" or
     * "mock" is excluded on the same terms.
     */
    public static boolean isSimulator(LLMProvider provider) {
        String name = provider.getProviderName();
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        return lower.contains("simulator") || lower.contains("mock");
    }

    /** Whether real providers only, or the Simulator too. */
    public enum Mode {
        REAL,
        SIMULATED
    }

    private ProviderCapability resolveCapability(String purpose) {
        if (purpose == null) return ProviderCapability.CHAT;

        switch (purpose.toLowerCase()) {
            case "code-generation":
            case "bug-analysis":
                return ProviderCapability.CODE_GENERATION;
            case "embedding":
                return ProviderCapability.EMBEDDING;
            case "vision":
                return ProviderCapability.VISION;
            default:
                return ProviderCapability.CHAT;
        }
    }
}