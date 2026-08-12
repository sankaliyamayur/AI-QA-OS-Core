package com.aiqaos.provider.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.ProviderCapability;
import com.aiqaos.provider.exception.AllProvidersExhaustedException;
import com.aiqaos.provider.exception.ProviderException;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import org.junit.jupiter.api.Test;

/** Unit tests for primary→fallback resilience (MNT-3). */
class LLMResilienceManagerTest {

    private final LLMResilienceManager manager = new LLMResilienceManager();

    private static LLMProvider ok(String name, String text) {
        return provider(name, () -> new LLMResponse(text, "m", new TokenUsage(1, 1), 1L));
    }

    private static LLMProvider failing(String name) {
        return provider(name, () -> { throw new RuntimeException("boom from " + name); });
    }

    private static LLMProvider provider(String name, java.util.function.Supplier<LLMResponse> behaviour) {
        return new LLMProvider() {
            @Override public LLMResponse generate(LLMRequest request) { return behaviour.get(); }
            @Override public String getProviderName() { return name; }
            @Override public boolean isAvailable() { return true; }
            @Override public boolean supports(ProviderCapability capability) { return true; }
        };
    }

    @Test
    void usesPrimaryWhenItSucceeds() {
        LLMResponse r = manager.executeWithFallback(ok("primary", "P"), ok("fallback", "F"), new LLMRequest());
        assertThat(r.getText()).isEqualTo("P");
    }

    @Test
    void fallsBackWhenPrimaryFails() {
        LLMResponse r = manager.executeWithFallback(failing("primary"), ok("fallback", "F"), new LLMRequest());
        assertThat(r.getText()).isEqualTo("F");
    }

    /**
     * The message changed with the failover chain: a spent chain now throws
     * {@link AllProvidersExhaustedException} (still a {@link ProviderException}) carrying the
     * {@code LLM_PROVIDERS_EXHAUSTED} marker and every attempt, instead of the old
     * "Fallback provider failed as well". The marker is load-bearing — the orchestrator matches on it
     * to refuse to report a run as successful when no provider could serve it.
     */
    @Test
    void throwsProviderExceptionWhenBothFail() {
        assertThatThrownBy(() ->
                manager.executeWithFallback(failing("primary"), failing("fallback"), new LLMRequest()))
                .isInstanceOf(ProviderException.class)
                .isInstanceOf(AllProvidersExhaustedException.class)
                .hasMessageContaining(AllProvidersExhaustedException.MARKER)
                .hasMessageContaining("primary")
                .hasMessageContaining("fallback");
    }
}
