package com.aiqaos.provider.contract;

import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;

public interface LLMProvider {
    LLMResponse generate(LLMRequest request);
    String getProviderName();
    boolean isAvailable();
}