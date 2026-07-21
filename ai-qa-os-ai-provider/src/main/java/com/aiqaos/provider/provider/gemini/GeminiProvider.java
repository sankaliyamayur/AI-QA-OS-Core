package com.aiqaos.provider.provider.gemini;

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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

@Component
public class GeminiProvider implements LLMProvider, StreamingLLMProvider {

    private static final String GENERATE_CONTENT_URL_TEMPLATE =
        "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private final SecretManager secretManager;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    /** Overridable so a retired model never requires a code change. */
    @Value("${aiqaos.provider.gemini.model:gemini-2.5-flash}")
    private String model;

    /**
     * Gemini 2.5 models spend reasoning tokens out of the same output budget, so an
     * unbounded thinking phase truncates the JSON mid-string. Capping thinking keeps
     * the remainder of the budget available for the actual response.
     */
    @Value("${aiqaos.provider.gemini.thinking-budget:1024}")
    private int thinkingBudget;

    /**
     * Floor for the output budget. Agents request 2048, but a full script suite is
     * one Playwright file per test case and truncates well before that.
     */
    @Value("${aiqaos.provider.gemini.max-output-tokens:32768}")
    private int maxOutputTokens;

    private final ApiKeyPool keyPool;

    public GeminiProvider(SecretManager secretManager,
                          ObjectMapper objectMapper,
                          @Value("${aiqaos.provider.gemini.key-cooldown-seconds:300}") long keyCooldownSeconds) {
        this.secretManager = secretManager;
        this.objectMapper = objectMapper;
        this.keyPool = new ApiKeyPool(secretManager, "GEMINI_API_KEY", Duration.ofSeconds(keyCooldownSeconds));

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(60_000);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        List<String> keys = keyPool.availableKeys();
        if (keys.isEmpty()) {
            throw new ProviderException("GEMINI_API_KEY is not configured");
        }

        ProviderException lastQuotaFailure = null;

        // Rotate only past keys that are themselves the problem. Any other error is a
        // property of the request or the service, so trying more keys just burns them.
        for (String key : keys) {
            try {
                return generateWithKey(request, key);
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                if (status == 429 || status == 403 || status == 401) {
                    keyPool.markExhausted(key);
                    lastQuotaFailure = new ProviderException(
                            "Gemini key " + ApiKeyPool.maskKey(key) + " rejected with status "
                                    + status + ": " + e.getResponseBodyAsString(), e);
                    continue;
                }
                throw new ProviderException(
                        "Gemini API call failed with status " + status + ": " + e.getResponseBodyAsString(), e);
            } catch (RestClientException e) {
                throw new ProviderException("Gemini API call failed: " + e.getMessage(), e);
            }
        }

        throw new ProviderException(
                "All " + keys.size() + " Gemini key(s) exhausted. Last failure: "
                        + (lastQuotaFailure == null ? "unknown" : lastQuotaFailure.getMessage()),
                lastQuotaFailure);
    }

    private LLMResponse generateWithKey(LLMRequest request, String key) {
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
        generationConfig.put("maxOutputTokens", Math.max(request.getMaxTokens(), maxOutputTokens));
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.putObject("thinkingConfig").put("thinkingBudget", thinkingBudget);

        String url = String.format(GENERATE_CONTENT_URL_TEMPLATE, model);

        // Rest client exceptions deliberately propagate: the caller inspects the status
        // code to decide whether the key is at fault and another one is worth trying.
        long start = System.currentTimeMillis();
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
        String respondingModel = responseBody.path("modelVersion").asText(model);

        return new LLMResponse(content, respondingModel, new TokenUsage(promptTokens, completionTokens), duration);
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
        return keyPool.hasKeys();
    }

    @Override
    public boolean supports(ProviderCapability capability) {
        return capability == ProviderCapability.CHAT ||
               capability == ProviderCapability.VISION ||
               capability == ProviderCapability.EMBEDDING;
    }
}
