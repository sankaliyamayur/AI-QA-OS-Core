package com.aiqaos.provider.manager;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.exception.ProviderException;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LLMResilienceManager {
    private static final Logger log = LoggerFactory.getLogger(LLMResilienceManager.class);

    public LLMResponse executeWithFallback(LLMProvider primary, LLMProvider fallback, LLMRequest request) {
        try {
            return primary.generate(request);
        } catch (Exception e) {
            // Log the cause, not just the fact — callers only ever see the wrapper message,
            // so an unlogged cause here is invisible everywhere downstream.
            log.warn("Primary provider {} failed, falling back to {}: {}",
                    primary.getProviderName(), fallback.getProviderName(), e.toString(), e);
            try {
                return fallback.generate(request);
            } catch (Exception ex) {
                log.error("Fallback provider {} also failed: {}", fallback.getProviderName(), ex.toString(), ex);
                throw new ProviderException(
                        "Fallback provider failed as well: " + ex.getMessage(), ex);
            }
        }
    }
}