package com.aiqaos.provider.provider.ollama;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.StreamingLLMProvider;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.exception.ProviderException;
import org.springframework.stereotype.Component;

import com.aiqaos.provider.contract.ProviderCapability;
import java.util.function.Consumer;

@Component
public class OllamaProvider implements LLMProvider, StreamingLLMProvider {

    @Override
    public LLMResponse generate(LLMRequest request) {
        throw new ProviderException("Ollama provider is not wired to a real API yet");
    }

    @Override
    public void stream(LLMRequest request, Consumer<String> tokenConsumer) {
        throw new ProviderException("Ollama provider is not wired to a real API yet");
    }

    @Override
    public String getProviderName() { return "Ollama"; }

    /**
     * Reports unavailable until this provider makes a real API call, so that
     * ModelRouter never selects it over a genuinely wired provider.
     */
    @Override
    public boolean isAvailable() { return false; }

    @Override
    public boolean supports(ProviderCapability capability) {
        return capability == ProviderCapability.CHAT ||
               capability == ProviderCapability.CODE_GENERATION;
    }
}