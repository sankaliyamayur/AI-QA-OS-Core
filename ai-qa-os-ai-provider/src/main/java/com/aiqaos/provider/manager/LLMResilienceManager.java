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
            log.warn("Primary provider {} failed. Falling back to {}...", primary.getProviderName(), fallback.getProviderName());
            try {
                return fallback.generate(request);
            } catch (Exception ex) {
                throw new ProviderException("Fallback provider failed as well", ex);
            }
        }
    }
}