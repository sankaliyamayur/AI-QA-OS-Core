package com.aiqaos.provider.provider.openai;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.ProviderCapability;
import com.aiqaos.provider.contract.StreamingLLMProvider;
import com.aiqaos.provider.exception.ProviderException;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import com.aiqaos.security.secret.SecretManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.function.Consumer;

@Component
public class OpenAIProvider implements LLMProvider, StreamingLLMProvider {

    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";

    private final SecretManager secretManager;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAIProvider(SecretManager secretManager, ObjectMapper objectMapper) {
        this.secretManager = secretManager;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(60_000);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        String key = secretManager.getSecret("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new ProviderException("OPENAI_API_KEY is not configured");
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", DEFAULT_MODEL);
        body.put("temperature", request.getTemperature());
        body.put("max_tokens", request.getMaxTokens());
        body.putObject("response_format").put("type", "json_object");

        ArrayNode messages = body.putArray("messages");
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
            ObjectNode systemMessage = messages.addObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", request.getSystemPrompt());
        }
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", request.getPrompt());

        long start = System.currentTimeMillis();
        try {
            JsonNode responseBody = restClient.post()
                .uri(CHAT_COMPLETIONS_URL)
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(JsonNode.class);
            long duration = System.currentTimeMillis() - start;

            String content = responseBody.path("choices").path(0).path("message").path("content").asText();
            long promptTokens = responseBody.path("usage").path("prompt_tokens").asLong(0);
            long completionTokens = responseBody.path("usage").path("completion_tokens").asLong(0);
            String model = responseBody.path("model").asText(DEFAULT_MODEL);

            return new LLMResponse(content, model, new TokenUsage(promptTokens, completionTokens), duration);
        } catch (RestClientResponseException e) {
            throw new ProviderException(
                "OpenAI API call failed with status " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new ProviderException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(LLMRequest request, Consumer<String> tokenConsumer) {
        LLMResponse response = generate(request);
        tokenConsumer.accept(response.getText());
    }

    @Override
    public String getProviderName() { return "OpenAI"; }

    @Override
    public boolean isAvailable() {
        String key = secretManager.getSecret("OPENAI_API_KEY");
        return key != null && !key.isBlank();
    }

    @Override
    public boolean supports(ProviderCapability capability) {
        return capability == ProviderCapability.CHAT ||
               capability == ProviderCapability.CODE_GENERATION ||
               capability == ProviderCapability.VISION ||
               capability == ProviderCapability.IMAGE ||
               capability == ProviderCapability.FUNCTION_CALLING ||
               capability == ProviderCapability.STREAMING ||
               capability == ProviderCapability.EMBEDDING;
    }
}
