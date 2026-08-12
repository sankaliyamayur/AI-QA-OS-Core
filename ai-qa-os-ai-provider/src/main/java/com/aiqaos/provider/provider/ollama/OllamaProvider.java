package com.aiqaos.provider.provider.ollama;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.ProviderCapability;
import com.aiqaos.provider.contract.StreamingLLMProvider;
import com.aiqaos.provider.exception.ProviderException;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * AI-5 (ADR-078): the local-model provider — a real client for a locally-run Ollama server
 * ({@code /api/generate}), so the platform can run without any cloud LLM key. Mirrors
 * {@code OpenAIProvider}. Opt-in ({@code aiqaos.provider.ollama.enabled}, default false) so a missing
 * local server never makes {@code ModelRouter} select it. Ollama returns real token counts
 * ({@code prompt_eval_count}/{@code eval_count}), keeping cost/AI-6 budgeting faithful.
 */
@Component
public class OllamaProvider implements LLMProvider, StreamingLLMProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String baseUrl;
    private final String model;

    /**
     * @param readTimeoutSeconds how long to wait for a completion. Configurable because the old
     *     hardcoded 120s is not a property of Ollama but of the machine running it: a local model is
     *     as fast as the CPU or GPU underneath. Measured on a GPU-less Intel i3-2100, qwen2.5:3b
     *     produces ~2.8 tokens/sec, so a 500-token script needs ~3 minutes and a 1500-token one
     *     ~9 — every one of which the 120s ceiling turned into a timeout, i.e. into a provider
     *     failure, for a model that was working correctly and merely slow. 600s suits CPU inference;
     *     lower it for a GPU host, where a hung request should be noticed quickly.
     */
    @Autowired
    public OllamaProvider(ObjectMapper objectMapper,
                          @Value("${aiqaos.provider.ollama.enabled:false}") boolean enabled,
                          @Value("${aiqaos.provider.ollama.base-url:http://localhost:11434}") String baseUrl,
                          @Value("${aiqaos.provider.ollama.model:llama3}") String model,
                          @Value("${aiqaos.provider.ollama.read-timeout-seconds:600}") int readTimeoutSeconds) {
        this(defaultRestClient(readTimeoutSeconds), objectMapper, enabled, baseUrl, model);
    }

    /** Test seam: inject a RestClient (e.g. bound to MockRestServiceServer). */
    OllamaProvider(RestClient restClient, ObjectMapper objectMapper, boolean enabled, String baseUrl, String model) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    private static RestClient defaultRestClient(int readTimeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // Connect stays short: a local server either accepts immediately or is not running.
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(Math.max(1, readTimeoutSeconds) * 1000);
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("prompt", request.getPrompt());
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
            body.put("system", request.getSystemPrompt());
        }
        body.put("stream", false);
        ObjectNode options = body.putObject("options");
        options.put("temperature", request.getTemperature());
        if (request.getMaxTokens() > 0) {
            options.put("num_predict", request.getMaxTokens());
        }

        long start = System.currentTimeMillis();
        JsonNode response;
        try {
            response = restClient.post()
                .uri(baseUrl + "/api/generate")
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new ProviderException("Ollama API call failed: " + e.getMessage(), e);
        }
        long duration = System.currentTimeMillis() - start;

        String content = response.path("response").asText();
        long promptTokens = response.path("prompt_eval_count").asLong(0);
        long completionTokens = response.path("eval_count").asLong(0);
        String respModel = response.path("model").asText(model);
        return new LLMResponse(content, respModel, new TokenUsage(promptTokens, completionTokens), duration);
    }

    @Override
    public void stream(LLMRequest request, Consumer<String> tokenConsumer) {
        tokenConsumer.accept(generate(request).getText());
    }

    @Override
    public String getProviderName() { return "Ollama"; }

    /** Opt-in: only selectable when explicitly enabled (a local Ollama server is expected to be up). */
    @Override
    public boolean isAvailable() { return enabled; }

    @Override
    public boolean supports(ProviderCapability capability) {
        return capability == ProviderCapability.CHAT ||
               capability == ProviderCapability.CODE_GENERATION;
    }
}
