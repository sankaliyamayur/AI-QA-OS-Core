package com.aiqaos.provider.cost;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * AI-6 (ADR-075): thread-safe in-memory accumulator of LLM <b>token</b> usage, keyed by scope, for
 * fast context/token-budget checks — the token counterpart to {@link SpendLedger}. Fed with the
 * <b>actual</b> token counts ({@code input + output}) from each {@code LLMResponse} by
 * {@link CostTracker}; the durable per-call record stays in {@code LLMCostEntity}. The global counter
 * rolls over daily. (Cross-restart seeding is FI-AI6-B, shared with ENT-3's FI-ENT3-A.)
 */
@Component
public class TokenLedger {

    private final Supplier<LocalDate> clock;
    private final Map<String, Long> workflowTokens = new ConcurrentHashMap<>();
    private final Map<String, Long> agentTokens = new ConcurrentHashMap<>();
    private final AtomicLong globalToday = new AtomicLong(0L);
    private volatile LocalDate globalDay;

    public TokenLedger() {
        this(LocalDate::now);
    }

    /** Test seam: supply a mutable clock to exercise the daily rollover. */
    TokenLedger(Supplier<LocalDate> clock) {
        this.clock = clock;
        this.globalDay = clock.get();
    }

    public void record(long tokens, String correlationId, String agentType) {
        rolloverIfNeeded();
        globalToday.addAndGet(tokens);
        if (correlationId != null) {
            workflowTokens.merge(correlationId, tokens, Long::sum);
        }
        if (agentType != null) {
            agentTokens.merge(agentType, tokens, Long::sum);
        }
    }

    public long globalToday() {
        rolloverIfNeeded();
        return globalToday.get();
    }

    public long workflow(String correlationId) {
        return correlationId == null ? 0L : workflowTokens.getOrDefault(correlationId, 0L);
    }

    public long agent(String agentType) {
        return agentType == null ? 0L : agentTokens.getOrDefault(agentType, 0L);
    }

    private synchronized void rolloverIfNeeded() {
        LocalDate now = clock.get();
        if (!now.equals(globalDay)) {
            globalDay = now;
            globalToday.set(0L);
        }
    }
}
