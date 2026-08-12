package com.aiqaos.provider.provider.ollama;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aiqaos.provider.exception.ProviderException;
import com.aiqaos.provider.model.LLMRequest;
import tools.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The read timeout must be configurable, because it is a property of the machine, not of Ollama.
 *
 * <p><b>Why.</b> The timeout was hardcoded at 120s. Measured on a GPU-less Intel i3-2100,
 * {@code qwen2.5:3b} generates ~2.8 tokens/sec, so a 500-token Playwright script takes ~3 minutes and
 * a 1500-token one ~9. Every one of those hit the ceiling and surfaced as a provider failure — for a
 * model that was working correctly and merely slow. Worse, under the failover chain a timeout is
 * retryable, so a slow-but-healthy local model would burn the whole chain and end in
 * AllProvidersExhaustedException.
 *
 * <p>Driven against a real HTTP server that delays its response, so this exercises the socket timeout
 * rather than asserting a field value.
 */
class OllamaReadTimeoutTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    /** A stand-in Ollama that waits {@code delayMillis} before answering. */
    private String startSlowServer(long delayMillis) throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/generate", exchange -> {
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = ("{\"response\":\"hello\",\"model\":\"test-model\","
                    + "\"prompt_eval_count\":1,\"eval_count\":1}").getBytes(StandardCharsets.UTF_8);
            // Without this the RestClient finds no converter for the JSON body and the test fails
            // for a reason that has nothing to do with timeouts.
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        return "http://localhost:" + server.getAddress().getPort();
    }

    private static LLMRequest request() {
        LLMRequest r = new LLMRequest();
        r.setPrompt("generate a script");
        return r;
    }

    @Test
    @DisplayName("a generation slower than the configured timeout fails")
    void tooSlowForTheConfiguredTimeoutFails() throws Exception {
        String baseUrl = startSlowServer(3_000);
        OllamaProvider provider = new OllamaProvider(new ObjectMapper(), true, baseUrl, "test-model", 1);

        assertThatThrownBy(() -> provider.generate(request()))
                .isInstanceOf(ProviderException.class);
    }

    @Test
    @DisplayName("the same slow generation succeeds once the timeout is raised")
    void raisingTheTimeoutLetsASlowModelFinish() throws Exception {
        String baseUrl = startSlowServer(3_000);
        OllamaProvider provider = new OllamaProvider(new ObjectMapper(), true, baseUrl, "test-model", 30);

        assertThat(provider.generate(request()).getText()).isEqualTo("hello");
    }

    @Test
    @DisplayName("the default is generous enough for CPU inference")
    void defaultTimeoutSuitsCpuInference() throws Exception {
        String baseUrl = startSlowServer(0);
        // Mirrors the @Value default of 600s.
        OllamaProvider provider = new OllamaProvider(new ObjectMapper(), true, baseUrl, "test-model", 600);

        assertThat(provider.generate(request()).getText()).isEqualTo("hello");
    }
}
