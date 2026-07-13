package com.aiqaos.provider.provider.openai;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.StreamingLLMProvider;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import com.aiqaos.security.secret.SecretManager;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class OpenAIProvider implements LLMProvider, StreamingLLMProvider {

    private final SecretManager secretManager;

    public OpenAIProvider(SecretManager secretManager) {
        this.secretManager = secretManager;
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        String key = secretManager.getSecret("OPENAI_API_KEY");
        // Mocks OpenAI Chat completion via WebClient
        long start = System.currentTimeMillis();
        String responseText = "OpenAI response to: " + request.getPrompt();
        long duration = System.currentTimeMillis() - start;

        return new LLMResponse(
            responseText, 
            "gpt-4o", 
            new TokenUsage(100, 150), 
            duration
        );
    }

    @Override
    public void stream(LLMRequest request, Consumer<String> tokenConsumer) {
        tokenConsumer.accept("OpenAI stream response chunk");
    }

    @Override
    public String getProviderName() { return "OpenAI"; }

    @Override
    public boolean isAvailable() { return true; }
}