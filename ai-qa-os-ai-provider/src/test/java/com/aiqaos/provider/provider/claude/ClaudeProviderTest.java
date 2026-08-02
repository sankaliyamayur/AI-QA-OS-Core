package com.aiqaos.provider.provider.claude;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.aiqaos.provider.exception.ProviderException;
import com.aiqaos.provider.key.ApiKeyPool;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.security.secret.SecretManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * AI-5 (ADR-078): the completed Claude provider maps the request onto the Anthropic Messages API
 * (headers + body) and the response back into LLMResponse (incl. token usage). MockRestServiceServer —
 * no live Anthropic key needed; the live call is user-run.
 */
class ClaudeProviderTest {

    /** Single-key SecretManager: ApiKeyPool reads baseName, baseName+"S", baseName+"_2..10". */
    private static ApiKeyPool pool(String value) {
        SecretManager sm = key -> "ANTHROPIC_API_KEY".equals(key) ? value : null;
        return new ApiKeyPool(sm, "ANTHROPIC_API_KEY", Duration.ofSeconds(300));
    }

    private static LLMRequest prompt(String p) {
        LLMRequest r = new LLMRequest();
        r.setPrompt(p);
        return r;
    }

    @Test
    void generate_mapsAnthropicRequestAndResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ClaudeProvider provider = new ClaudeProvider(
                builder.build(), new ObjectMapper(), "claude-3-5-sonnet-latest", pool("sk-test"));

        server.expect(requestTo("https://api.anthropic.com/v1/messages"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("x-api-key", "sk-test"))
                .andExpect(header("anthropic-version", "2023-06-01"))
                .andExpect(jsonPath("$.model").value("claude-3-5-sonnet-latest"))
                .andExpect(jsonPath("$.max_tokens").value(2048))   // LLMRequest default maxTokens
                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[0].content").value("hi"))
                .andRespond(withSuccess(
                        "{\"content\":[{\"type\":\"text\",\"text\":\"hello\"}],"
                                + "\"model\":\"claude-3-5-sonnet-latest\","
                                + "\"usage\":{\"input_tokens\":11,\"output_tokens\":22}}",
                        MediaType.APPLICATION_JSON));

        LLMResponse resp = provider.generate(prompt("hi"));

        server.verify();
        assertEquals("hello", resp.getText());
        assertEquals(11, resp.getUsage().getInputTokens());
        assertEquals(22, resp.getUsage().getOutputTokens());
    }

    @Test
    void isAvailable_reflectsKeyPresence() {
        assertTrue(new ClaudeProvider(RestClient.builder().build(), new ObjectMapper(), "m", pool("k")).isAvailable());
        assertFalse(new ClaudeProvider(RestClient.builder().build(), new ObjectMapper(), "m", pool(null)).isAvailable());
    }

    @Test
    void noKey_throws() {
        ClaudeProvider p = new ClaudeProvider(RestClient.builder().build(), new ObjectMapper(), "m", pool(null));
        assertThrows(ProviderException.class, () -> p.generate(prompt("hi")));
    }
}
