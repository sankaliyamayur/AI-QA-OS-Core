package com.aiqaos.provider.provider.claude;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.StreamingLLMProvider;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import com.aiqaos.security.secret.SecretManager;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class ClaudeProvider implements LLMProvider, StreamingLLMProvider {

    private final SecretManager secretManager;

    public ClaudeProvider(SecretManager secretManager) {
        this.secretManager = secretManager;
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        String key = secretManager.getSecret("CLAUDE_API_KEY");
        long start = System.currentTimeMillis();
        String responseText = "Claude response to: " + request.getPrompt();
        long duration = System.currentTimeMillis() - start;

        return new LLMResponse(
            responseText, 
            "claude-3-5-sonnet", 
            new TokenUsage(120, 180), 
            duration
        );
    }

    @Override
    public void stream(LLMRequest request, Consumer<String> tokenConsumer) {
        tokenConsumer.accept("Claude stream response chunk");
    }

    @Override
    public String getProviderName() { return "Claude"; }

    @Override
    public boolean isAvailable() { return true; }
}