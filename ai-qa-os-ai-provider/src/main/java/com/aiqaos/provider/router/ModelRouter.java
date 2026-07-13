package com.aiqaos.provider.router;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.ProviderCapability;
import org.springframework.stereotype.Component;

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