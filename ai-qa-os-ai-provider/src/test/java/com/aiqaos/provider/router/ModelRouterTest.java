package com.aiqaos.provider.router;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.ProviderCapability;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Unit tests for provider selection (MNT-3). Hand-written stub providers — no Mockito. */
class ModelRouterTest {

    /** A stub provider with configurable name/availability/capabilities. */
    private static LLMProvider provider(String name, boolean available, ProviderCapability... caps) {
        Set<ProviderCapability> supported = Set.of(caps);
        return new LLMProvider() {
            @Override public LLMResponse generate(LLMRequest request) { return null; }
            @Override public String getProviderName() { return name; }
            @Override public boolean isAvailable() { return available; }
            @Override public boolean supports(ProviderCapability capability) { return supported.contains(capability); }
        };
    }

    @Test
    void routesCodePurposeToACodeCapableProvider() {
        ModelRouter router = new ModelRouter(List.of(
                provider("Chatty", true, ProviderCapability.CHAT),
                provider("Coder", true, ProviderCapability.CODE_GENERATION)));

        assertThat(router.routeModel("code-generation")).isEqualTo("Coder");
        assertThat(router.routeModel("bug-analysis")).isEqualTo("Coder");
    }

    @Test
    void routesChatAndUnknownPurposesToAChatProvider() {
        ModelRouter router = new ModelRouter(List.of(
                provider("Coder", true, ProviderCapability.CODE_GENERATION),
                provider("Chatty", true, ProviderCapability.CHAT)));

        assertThat(router.routeModel("something-else")).isEqualTo("Chatty");
        assertThat(router.routeModel(null)).isEqualTo("Chatty");
    }

    @Test
    void skipsUnavailableProviders() {
        ModelRouter router = new ModelRouter(List.of(
                provider("CoderDown", false, ProviderCapability.CODE_GENERATION),
                provider("CoderUp", true, ProviderCapability.CODE_GENERATION)));

        assertThat(router.routeModel("code-generation")).isEqualTo("CoderUp");
    }

    @Test
    void fallsBackToGeminiWhenNoProviderMatches() {
        ModelRouter router = new ModelRouter(List.of(
                provider("ChatOnly", true, ProviderCapability.CHAT)));

        assertThat(router.routeModel("embedding")).isEqualTo("Gemini");
    }
}
