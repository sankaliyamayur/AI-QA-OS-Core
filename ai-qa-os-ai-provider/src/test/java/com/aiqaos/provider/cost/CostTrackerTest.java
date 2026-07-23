package com.aiqaos.provider.cost;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.aiqaos.observability.entity.AgentTraceEntity;
import com.aiqaos.observability.entity.LLMCostEntity;
import com.aiqaos.observability.repository.AgentTraceRepository;
import com.aiqaos.observability.repository.LLMCostRepository;
import com.aiqaos.provider.model.LLMRequest;
import com.aiqaos.provider.model.LLMResponse;
import com.aiqaos.provider.model.TokenUsage;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for cost calculation + persistence (MNT-3). The JPA repositories are stubbed with a
 * JDK dynamic proxy that captures {@code save(...)} and returns defaults for everything else —
 * no Mockito, no database.
 */
class CostTrackerTest {

    private final List<Object> saved = new ArrayList<>();

    private LLMCostRepository costRepo() {
        return proxy(LLMCostRepository.class);
    }

    private AgentTraceRepository traceRepo() {
        return proxy(AgentTraceRepository.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> iface) {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class[]{iface}, (p, method, args) -> {
            if ("save".equals(method.getName()) && args != null && args.length == 1) {
                saved.add(args[0]);
                return args[0];
            }
            Class<?> rt = method.getReturnType();
            if (rt == boolean.class) return false;
            if (rt == long.class) return 0L;
            if (rt == int.class) return 0;
            if (rt == Optional.class) return Optional.empty();
            if (rt == List.class) return List.of();
            return null;
        });
    }

    private LLMCostEntity savedCost() {
        return saved.stream().filter(o -> o instanceof LLMCostEntity)
                .map(o -> (LLMCostEntity) o).findFirst().orElseThrow();
    }

    private static LLMRequest request() {
        LLMRequest req = new LLMRequest();
        req.setCorrelationId("corr-1");
        req.setAgentType("TEST_CASE_GENERATOR");
        req.setPurpose("chat");
        req.setPrompt("hello");
        return req;
    }

    private static LLMResponse response(String model, long in, long out) {
        return new LLMResponse("world", model, new TokenUsage(in, out), 42L);
    }

    @Test
    void computesDefaultRateCostAndPersistsBothRows() {
        CostTracker tracker = new CostTracker(costRepo(), traceRepo());

        // default rates 5.0 / 15.0 per 1M tokens → (1e6*5 + 1e6*15) / 1e6 = 20.0
        tracker.track(request(), response("gpt-4o", 1_000_000, 1_000_000), "openai");

        LLMCostEntity cost = savedCost();
        assertThat(cost.getCost()).isCloseTo(20.0, within(1e-9));
        assertThat(cost.getProvider()).isEqualTo("openai");
        assertThat(cost.getInputTokens()).isEqualTo(1_000_000);
        assertThat(saved).anyMatch(o -> o instanceof AgentTraceEntity);
    }

    @Test
    void appliesMiniRates() {
        CostTracker tracker = new CostTracker(costRepo(), traceRepo());

        // "mini" rates 0.15 / 0.60 → (1e6*0.15 + 1e6*0.60) / 1e6 = 0.75
        tracker.track(request(), response("gpt-4o-mini", 1_000_000, 1_000_000), "openai");

        assertThat(savedCost().getCost()).isCloseTo(0.75, within(1e-9));
    }

    @Test
    void appliesFlashRates() {
        CostTracker tracker = new CostTracker(costRepo(), traceRepo());

        // "flash" rates 0.35 / 1.05 → (1e6*0.35 + 1e6*1.05) / 1e6 = 1.40.
        // NB: the model name must not contain "mini" — and "geMINI" does, so any Gemini model is
        // (mis)priced as "mini" by the current substring logic. Tracked as FI-MNT3-C.
        tracker.track(request(), response("vertex-flash", 1_000_000, 1_000_000), "google");

        assertThat(savedCost().getCost()).isCloseTo(1.40, within(1e-9));
    }
}
