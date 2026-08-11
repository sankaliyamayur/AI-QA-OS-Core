package com.aiqaos.provider.provider.ollama;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * AI-5 (ADR-078): the local-model provider maps the request onto Ollama's /api/generate and the
 * response back into LLMResponse (incl. real token counts). Uses MockRestServiceServer — no live
 * Ollama server needed; the live round-trip is user-run.
 */
class OllamaProviderTest {

    @Test
    void generate_mapsRequestBodyAndResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OllamaProvider provider = new OllamaProvider(
                builder.build(), new ObjectMapper(), true, "http://localhost:11434", "llama3");

        server.expect(requestTo("http://localhost:11434/api/generate"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("llama3"))
                .andExpect(jsonPath("$.prompt").value("hi"))
                .andExpect(jsonPath("$.stream").value(false))
                .andRespond(withSuccess(
                        "{\"response\":\"hello\",\"model\":\"llama3\",\"prompt_eval_count\":5,\"eval_count\":7}",
                        MediaType.APPLICATION_JSON));

        LLMRequest req = new LLMRequest();
        req.setPrompt("hi");
        LLMResponse resp = provider.generate(req);

        server.verify();
        assertEquals("hello", resp.getText());
        assertEquals("llama3", resp.getModel());
        assertEquals(5, resp.getUsage().getInputTokens());
        assertEquals(7, resp.getUsage().getOutputTokens());
    }

    @Test
    void isAvailable_reflectsEnabledFlag_andName() {
        RestClient c = RestClient.builder().build();
        assertFalse(new OllamaProvider(c, new ObjectMapper(), false, "x", "m").isAvailable());
        assertTrue(new OllamaProvider(c, new ObjectMapper(), true, "x", "m").isAvailable());
        assertEquals("Ollama", new OllamaProvider(c, new ObjectMapper(), true, "x", "m").getProviderName());
    }
}
