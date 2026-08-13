package com.aiqaos.provider.provider.gemini;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.aiqaos.provider.exception.ProviderException;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.security.secret.SecretManager;
import tools.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GeminiProviderFinishReasonTest {

    private SecretManager secretManager;
    private ObjectMapper objectMapper;
    private RestClient.Builder restClientBuilder;
    private MockRestServiceServer server;
    private GeminiProvider provider;

    @BeforeEach
    void setUp() throws Exception {
        secretManager = key -> "GEMINI_API_KEY".equals(key) ? "test-key-123" : null;
        objectMapper = new ObjectMapper();
        restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();

        provider = new GeminiProvider(secretManager, objectMapper, 300);
        setField(provider, "restClient", restClientBuilder.build());
        setField(provider, "model", "gemini-1.5-flash");
        setField(provider, "maxOutputTokens", 32768);
    }

    private static void setField(Object target, String fieldName, Object val) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, val);
    }

    @Test
    @DisplayName("MAX_TOKENS finishReason throws ProviderException instead of returning partial string")
    void maxTokensFinishReasonThrowsProviderException() {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
        String jsonResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [{ "text": "{\\"partial\\":\\"data" }]
                  },
                  "finishReason": "MAX_TOKENS"
                }
              ]
            }
            """;

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-key-123"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        LLMRequest request = new LLMRequest("Analyze requirement");
        request.setMaxTokens(4096);

        ProviderException ex = assertThrows(ProviderException.class, () -> provider.generate(request));
        assertTrue(ex.getMessage().contains("MAX_TOKENS"), "Expected exception message to mention MAX_TOKENS: " + ex.getMessage());
    }

    @Test
    @DisplayName("STOP finishReason returns normal content successfully")
    void stopFinishReasonReturnsContentSuccessfully() {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
        String jsonResponse = """
            {
              "candidates": [
                {
                  "content": {
                    "parts": [{ "text": "{\\"status\\":\\"complete\\"}" }]
                  },
                  "finishReason": "STOP"
                }
              ]
            }
            """;

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-key-123"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        LLMRequest request = new LLMRequest("Analyze requirement");
        LLMResponse response = provider.generate(request);

        assertEquals("{\"status\":\"complete\"}", response.getText());
    }

    @Test
    @DisplayName("Empty candidates array throws ProviderException instead of NullPointerException")
    void emptyCandidatesThrowsExplicitProviderException() {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
        String jsonResponse = "{\"candidates\":[]}";

        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "test-key-123"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        LLMRequest request = new LLMRequest("Analyze requirement");

        ProviderException ex = assertThrows(ProviderException.class, () -> provider.generate(request));
        assertTrue(ex.getMessage().contains("no candidates"), "Expected exception message to mention no candidates: " + ex.getMessage());
    }
}
