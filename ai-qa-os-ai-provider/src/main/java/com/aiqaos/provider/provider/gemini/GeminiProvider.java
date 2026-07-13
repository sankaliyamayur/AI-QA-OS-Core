package com.aiqaos.provider.provider.gemini;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.StreamingLLMProvider;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import com.aiqaos.security.secret.SecretManager;
import org.springframework.stereotype.Component;

import com.aiqaos.provider.contract.ProviderCapability;
import java.util.function.Consumer;

@Component
public class GeminiProvider implements LLMProvider, StreamingLLMProvider {

    private final SecretManager secretManager;

    public GeminiProvider(SecretManager secretManager) {
        this.secretManager = secretManager;
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        String key = secretManager.getSecret("GEMINI_API_KEY");
        long start = System.currentTimeMillis();
        String responseText = "Gemini response to: " + request.getPrompt();
        long duration = System.currentTimeMillis() - start;

        return new LLMResponse(
            responseText, 
            "gemini-1.5-flash", 
            new TokenUsage(80, 110), 
            duration
        );
    }

    @Override
    public void stream(LLMRequest request, Consumer<String> tokenConsumer) {
        tokenConsumer.accept("Gemini stream response chunk");
    }

    @Override
    public String getProviderName() { return "Gemini"; }

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public boolean supports(ProviderCapability capability) {
        return capability == ProviderCapability.CHAT ||
               capability == ProviderCapability.VISION ||
               capability == ProviderCapability.EMBEDDING;
    }
}