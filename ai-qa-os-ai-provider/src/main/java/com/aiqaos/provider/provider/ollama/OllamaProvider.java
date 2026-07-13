package com.aiqaos.provider.provider.ollama;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.StreamingLLMProvider;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class OllamaProvider implements LLMProvider, StreamingLLMProvider {

    @Override
    public LLMResponse generate(LLMRequest request) {
        long start = System.currentTimeMillis();
        String responseText = "Ollama local response to: " + request.getPrompt();
        long duration = System.currentTimeMillis() - start;

        return new LLMResponse(
            responseText, 
            "llama3", 
            new TokenUsage(150, 200), 
            duration
        );
    }

    @Override
    public void stream(LLMRequest request, Consumer<String> tokenConsumer) {
        tokenConsumer.accept("Ollama stream response chunk");
    }

    @Override
    public String getProviderName() { return "Ollama"; }

    @Override
    public boolean isAvailable() { return true; }
}