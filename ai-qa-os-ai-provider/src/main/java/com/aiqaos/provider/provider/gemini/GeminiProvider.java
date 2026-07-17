package com.aiqaos.provider.provider.gemini;

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
public class GeminiProvider implements LLMProvider, StreamingLLMProvider {

    private static final String GENERATE_CONTENT_URL_TEMPLATE =
        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    private static final String DEFAULT_MODEL = "gemini-1.5-flash";

    private final SecretManager secretManager;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public GeminiProvider(SecretManager secretManager, ObjectMapper objectMapper) {
        this.secretManager = secretManager;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(60_000);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        String key = secretManager.getSecret("GEMINI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new ProviderException("GEMINI_API_KEY is not configured");
        }

        ObjectNode body = objectMapper.createObjectNode();

        ArrayNode contents = body.putArray("contents");
        ObjectNode userContent = contents.addObject();
        userContent.put("role", "user");
        userContent.putArray("parts").addObject().put("text", request.getPrompt());

        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
            ObjectNode systemInstruction = body.putObject("systemInstruction");
            systemInstruction.putArray("parts").addObject().put("text", request.getSystemPrompt());
        }

        ObjectNode generationConfig = body.putObject("generationConfig");
        generationConfig.put("temperature", request.getTemperature());
        generationConfig.put("maxOutputTokens", request.getMaxTokens());
        generationConfig.put("responseMimeType", "application/json");

        String url = String.format(GENERATE_CONTENT_URL_TEMPLATE, DEFAULT_MODEL);

        long start = System.currentTimeMillis();
        try {
            JsonNode responseBody = restClient.post()
                .uri(url)
                .header("x-goog-api-key", key)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(JsonNode.class);
            long duration = System.currentTimeMillis() - start;

            String content = responseBody.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
            long promptTokens = responseBody.path("usageMetadata").path("promptTokenCount").asLong(0);
            long completionTokens = responseBody.path("usageMetadata").path("candidatesTokenCount").asLong(0);
            String model = responseBody.path("modelVersion").asText(DEFAULT_MODEL);

            return new LLMResponse(content, model, new TokenUsage(promptTokens, completionTokens), duration);
        } catch (RestClientResponseException e) {
            throw new ProviderException(
                "Gemini API call failed with status " + e.getStatusCode().value() + ": " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new ProviderException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(LLMRequest request, Consumer<String> tokenConsumer) {
        LLMResponse response = generate(request);
        tokenConsumer.accept(response.getText());
    }

    @Override
    public String getProviderName() { return "Gemini"; }

    @Override
    public boolean isAvailable() {
        String key = secretManager.getSecret("GEMINI_API_KEY");
        return key != null && !key.isBlank();
    }

    @Override
    public boolean supports(ProviderCapability capability) {
        return capability == ProviderCapability.CHAT ||
               capability == ProviderCapability.VISION ||
               capability == ProviderCapability.EMBEDDING;
    }
}
