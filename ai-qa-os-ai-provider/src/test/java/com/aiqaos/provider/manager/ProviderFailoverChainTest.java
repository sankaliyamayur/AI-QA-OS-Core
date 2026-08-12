package com.aiqaos.provider.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.provider.contract.LLMProvider;
import com.aiqaos.provider.contract.ProviderCapability;
import com.aiqaos.provider.exception.AllProvidersExhaustedException;
import com.aiqaos.provider.exception.ProviderException;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import com.aiqaos.provider.router.ModelRouter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The failover contract: OpenAI → Claude → Gemini, and never the Simulator during a real run.
 *
 * <p>What this replaces: {@code executeWithFallback} took exactly one fallback, and
 * {@code selectFallback} filled that slot with the Simulator ahead of every real provider. A
 * rate-limited OpenAI therefore returned a canned Simulator answer that the pipeline reported as a
 * pass. Two rows in the live database carry the fingerprint — {@code provider='Gemini'} recorded
 * against {@code model='local-simulator-v1'}.
 */
class ProviderFailoverChainTest {

    private final LLMResilienceManager manager = new LLMResilienceManager();

    /** Fails with a given status, or answers, and records whether it was called. */
    private static final class FakeProvider implements LLMProvider {
        private final String name;
        private final Integer failStatus;
        private final boolean available;
        int calls;

        FakeProvider(String name, Integer failStatus) {
            this(name, failStatus, true);
        }

        FakeProvider(String name, Integer failStatus, boolean available) {
            this.name = name;
            this.failStatus = failStatus;
            this.available = available;
        }

        @Override
        public LLMResponse generate(LLMRequest request) {
            calls++;
            if (failStatus != null) {
                throw new ProviderException(name + " failed", null, failStatus);
            }
            return new LLMResponse("ok-from-" + name, name + "-model", new TokenUsage(1, 1), 1L);
        }

        @Override
        public String getProviderName() {
            return name;
        }

        @Override
        public boolean isAvailable() {
            return available;
        }

        @Override
        public boolean supports(ProviderCapability capability) {
            return true;
        }
    }

    private static LLMRequest request() {
        LLMRequest r = new LLMRequest();
        r.setPrompt("hello");
        return r;
    }

    // ── the scenarios from the requirement ────────────────────────────────────────────────────────

    @Test
    void openAiRateLimitedFailsOverToClaudeAndContinues() {
        FakeProvider openai = new FakeProvider("openai", 429);
        FakeProvider claude = new FakeProvider("claude", null);
        FakeProvider gemini = new FakeProvider("gemini", null);

        LLMResilienceManager.ChainResult result =
                manager.executeChain(List.of(openai, claude, gemini), request(), false, 0L);

        assertEquals("claude", result.servingProvider().getProviderName());
        assertEquals("ok-from-claude", result.response().getText());
        assertEquals(0, gemini.calls, "gemini must not be called once claude succeeded");
    }

    @Test
    void openAiAndClaudeBothFailSoGeminiServes() {
        FakeProvider openai = new FakeProvider("openai", 429);
        FakeProvider claude = new FakeProvider("claude", 401);
        FakeProvider gemini = new FakeProvider("gemini", null);

        LLMResilienceManager.ChainResult result =
                manager.executeChain(List.of(openai, claude, gemini), request(), false, 0L);

        assertEquals("gemini", result.servingProvider().getProviderName());
        assertEquals(1, openai.calls);
        assertEquals(1, claude.calls);
    }

    @Test
    void allProvidersFailingThrowsExhaustedListingEveryAttempt() {
        FakeProvider openai = new FakeProvider("openai", 429);
        FakeProvider claude = new FakeProvider("claude", 503);
        FakeProvider gemini = new FakeProvider("gemini", 500);

        AllProvidersExhaustedException e = assertThrows(AllProvidersExhaustedException.class,
                () -> manager.executeChain(List.of(openai, claude, gemini), request(), false, 0L));

        assertEquals(3, e.getAttempts().size());
        assertTrue(e.getMessage().contains("openai"));
        assertTrue(e.getMessage().contains("claude"));
        assertTrue(e.getMessage().contains("gemini"));
        assertTrue(e.getMessage().contains(AllProvidersExhaustedException.MARKER),
                "the marker is what the orchestrator matches on to refuse a false green run");
    }

    @Test
    void anEmptyChainIsExhaustionNotSuccess() {
        assertThrows(AllProvidersExhaustedException.class,
                () -> manager.executeChain(List.of(), request(), false, 0L));
    }

    // ── error classification ──────────────────────────────────────────────────────────────────────

    @Test
    void aTerminalErrorStopsTheChainInsteadOfBurningEveryProvider() {
        FakeProvider openai = new FakeProvider("openai", 400);
        FakeProvider claude = new FakeProvider("claude", null);

        ProviderException e = assertThrows(ProviderException.class,
                () -> manager.executeChain(List.of(openai, claude), request(), false, 0L));

        assertEquals(400, e.getStatus());
        assertEquals(0, claude.calls,
                "a malformed request fails identically everywhere — failing over just wastes quota");
    }

    @Test
    void timeoutsAndConnectionFailuresAreRetryableBecauseTheySayNothingAboutTheRequest() {
        assertTrue(new ProviderException("timeout").isRetryable());
        assertTrue(new ProviderException("connection refused", new RuntimeException()).isRetryable());
        assertTrue(new ProviderException("rate limited", null, 429).isRetryable());
        assertTrue(new ProviderException("server error", null, 503).isRetryable());
        assertFalse(new ProviderException("bad request", null, 400).isRetryable());
        assertFalse(new ProviderException("not found", null, 404).isRetryable());
    }

    @Test
    void aProviderThrowingSomethingUnexpectedDoesNotBreakTheChain() {
        LLMProvider rogue = new LLMProvider() {
            @Override
            public LLMResponse generate(LLMRequest request) {
                throw new IllegalStateException("boom");
            }

            @Override
            public String getProviderName() {
                return "rogue";
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public boolean supports(ProviderCapability capability) {
                return true;
            }
        };
        FakeProvider claude = new FakeProvider("claude", null);

        LLMResilienceManager.ChainResult result =
                manager.executeChain(List.of(rogue, claude), request(), false, 0L);

        assertEquals("claude", result.servingProvider().getProviderName());
    }

    // ── the Simulator exclusion ───────────────────────────────────────────────────────────────────

    @Test
    void inRealModeTheSimulatorIsNotEvenInTheChain() {
        FakeProvider openai = new FakeProvider("openai", null);
        FakeProvider simulator = new FakeProvider("simulator", null);
        ModelRouter router = new ModelRouter(List.of(openai, simulator));

        List<LLMProvider> chain = router.routeChain("chat", List.of("openai", "simulator"), ModelRouter.Mode.REAL);

        assertEquals(1, chain.size());
        assertEquals("openai", chain.get(0).getProviderName());
    }

    @Test
    void inRealModeAnAllFailingChainCannotReachTheSimulator() {
        FakeProvider openai = new FakeProvider("openai", 429);
        FakeProvider claude = new FakeProvider("claude", 429);
        FakeProvider gemini = new FakeProvider("gemini", 429);
        FakeProvider simulator = new FakeProvider("simulator", null);
        ModelRouter router = new ModelRouter(List.of(openai, claude, gemini, simulator));

        List<LLMProvider> chain =
                router.routeChain("chat", List.of("openai", "claude", "gemini"), ModelRouter.Mode.REAL);

        assertThrows(AllProvidersExhaustedException.class,
                () -> manager.executeChain(chain, request(), false, 0L));
        assertEquals(0, simulator.calls, "THE false-green bug: the simulator must never rescue a real run");
    }

    @Test
    void inSimulatedModeTheSimulatorIsAvailableOnPurpose() {
        FakeProvider simulator = new FakeProvider("simulator", null);
        ModelRouter router = new ModelRouter(List.of(simulator));

        List<LLMProvider> chain = router.routeChain("chat", List.of("simulator"), ModelRouter.Mode.SIMULATED);

        assertEquals(1, chain.size());
        assertEquals("simulator", manager.executeChain(chain, request(), false, 0L)
                .servingProvider().getProviderName());
    }

    @Test
    void chainHonoursConfiguredPriorityOrder() {
        FakeProvider openai = new FakeProvider("openai", null);
        FakeProvider claude = new FakeProvider("claude", null);
        FakeProvider gemini = new FakeProvider("gemini", null);
        // Registered gemini-first to prove ordering comes from config, not bean order.
        ModelRouter router = new ModelRouter(List.of(gemini, claude, openai));

        List<LLMProvider> chain =
                router.routeChain("chat", List.of("openai", "claude", "gemini"), ModelRouter.Mode.REAL);

        List<String> names = new ArrayList<>();
        chain.forEach(p -> names.add(p.getProviderName()));
        assertEquals(List.of("openai", "claude", "gemini"), names);
    }

    @Test
    void unavailableProvidersAreExcludedSoAKeylessProviderIsNeverTried() {
        FakeProvider openai = new FakeProvider("openai", null, false);
        FakeProvider claude = new FakeProvider("claude", null, true);
        ModelRouter router = new ModelRouter(List.of(openai, claude));

        List<LLMProvider> chain =
                router.routeChain("chat", List.of("openai", "claude"), ModelRouter.Mode.REAL);

        assertEquals(1, chain.size());
        assertEquals("claude", chain.get(0).getProviderName());
    }
}
