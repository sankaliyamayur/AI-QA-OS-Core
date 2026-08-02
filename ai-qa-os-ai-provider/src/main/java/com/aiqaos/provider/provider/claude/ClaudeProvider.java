package com.aiqaos.provider.provider.claude;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.ProviderCapability;
import com.aiqaos.provider.contract.StreamingLLMProvider;
import com.aiqaos.provider.exception.ProviderException;
import com.aiqaos.provider.key.ApiKeyPool;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import com.aiqaos.security.secret.SecretManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * AI-5 (ADR-078): completes the Claude provider — a real client for the Anthropic <b>Messages API</b>
 * ({@code /v1/messages}), mirroring {@code OpenAIProvider} (RestClient + {@link ApiKeyPool} on
 * {@code ANTHROPIC_API_KEY}, key-rotation on 401/403/429, JSON → {@link LLMResponse} with real token
 * usage). Reports {@link #isAvailable()} false until a key is configured, so {@code ModelRouter} never
 * selects it over a genuinely usable provider.
 */
@Component
public class ClaudeProvider implements LLMProvider, StreamingLLMProvider {

    private static final String MESSAGES_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final int DEFAULT_MAX_TOKENS = 4096;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final ApiKeyPool keyPool;
    private final String model;

    @Autowired
    public ClaudeProvider(SecretManager secretManager,
                          ObjectMapper objectMapper,
                          @Value("${aiqaos.provider.claude.model:claude-3-5-sonnet-latest}") String model,
                          @Value("${aiqaos.provider.claude.key-cooldown-seconds:300}") long keyCooldownSeconds) {
        this(defaultRestClient(), objectMapper, model,
                new ApiKeyPool(secretManager, "ANTHROPIC_API_KEY", Duration.ofSeconds(keyCooldownSeconds)));
    }

    /** Test seam: inject a RestClient (e.g. bound to MockRestServiceServer) + a key pool. */
    ClaudeProvider(RestClient restClient, ObjectMapper objectMapper, String model, ApiKeyPool keyPool) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.model = model;
        this.keyPool = keyPool;
    }

    private static RestClient defaultRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(60_000);
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        List<String> keys = keyPool.availableKeys();
        if (keys.isEmpty()) {
            throw new ProviderException("ANTHROPIC_API_KEY is not configured");
        }

        ProviderException lastQuotaFailure = null;
        for (String key : keys) {
            try {
                return generateWithKey(request, key);
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                if (status == 429 || status == 403 || status == 401) {
                    keyPool.markExhausted(key);
                    lastQuotaFailure = new ProviderException(
                            "Anthropic key " + ApiKeyPool.maskKey(key) + " rejected with status "
                                    + status + ": " + e.getResponseBodyAsString(), e);
                    continue;
                }
                throw new ProviderException(
                        "Anthropic API call failed with status " + status + ": " + e.getResponseBodyAsString(), e);
            } catch (RestClientException e) {
                throw new ProviderException("Anthropic API call failed: " + e.getMessage(), e);
            }
        }

        throw new ProviderException(
                "All " + keys.size() + " Anthropic key(s) exhausted. Last failure: "
                        + (lastQuotaFailure == null ? "unknown" : lastQuotaFailure.getMessage()),
                lastQuotaFailure);
    }

    private LLMResponse generateWithKey(LLMRequest request, String key) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("max_tokens", request.getMaxTokens() > 0 ? request.getMaxTokens() : DEFAULT_MAX_TOKENS);
        body.put("temperature", request.getTemperature());
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
            body.put("system", request.getSystemPrompt());   // Anthropic: system is a top-level field
        }
        ArrayNode messages = body.putArray("messages");
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", request.getPrompt());

        long start = System.currentTimeMillis();
        JsonNode responseBody = restClient.post()
            .uri(MESSAGES_URL)
            .header("x-api-key", key)
            .header("anthropic-version", ANTHROPIC_VERSION)
            .header("content-type", "application/json")
            .body(body)
            .retrieve()
            .body(JsonNode.class);
        long duration = System.currentTimeMillis() - start;

        // Anthropic returns content as an array of typed blocks; take the first text block.
        String content = responseBody.path("content").path(0).path("text").asText();
        long inputTokens = responseBody.path("usage").path("input_tokens").asLong(0);
        long outputTokens = responseBody.path("usage").path("output_tokens").asLong(0);
        String respModel = responseBody.path("model").asText(model);
        return new LLMResponse(content, respModel, new TokenUsage(inputTokens, outputTokens), duration);
    }

    @Override
    public void stream(LLMRequest request, Consumer<String> tokenConsumer) {
        tokenConsumer.accept(generate(request).getText());
    }

    @Override
    public String getProviderName() { return "Claude"; }

    @Override
    public boolean isAvailable() { return keyPool.hasKeys(); }

    @Override
    public boolean supports(ProviderCapability capability) {
        return capability == ProviderCapability.CHAT ||
               capability == ProviderCapability.CODE_GENERATION ||
               capability == ProviderCapability.VISION ||
               capability == ProviderCapability.STREAMING;
    }
}
