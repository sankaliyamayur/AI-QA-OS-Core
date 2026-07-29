package com.aiqaos.provider.cost;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * ENT-3: thread-safe in-memory accumulator of LLM spend, keyed by scope, for fast quota checks. The
 * durable record stays in {@code LLMCostEntity} (written by {@link CostTracker}); this is the hot
 * enforcement view. The global counter rolls over daily. (Cross-restart seeding is FI-ENT3-A.)
 */
@Component
public class SpendLedger {

    private final Supplier<LocalDate> clock;
    private final Map<String, Double> workflowSpend = new ConcurrentHashMap<>();
    private final Map<String, Double> agentSpend = new ConcurrentHashMap<>();
    private final AtomicReference<Double> globalToday = new AtomicReference<>(0.0);
    private volatile LocalDate globalDay;

    public SpendLedger() {
        this(LocalDate::now);
    }

    /** Test seam: supply a mutable clock to exercise the daily rollover. */
    SpendLedger(Supplier<LocalDate> clock) {
        this.clock = clock;
        this.globalDay = clock.get();
    }

    public void record(double cost, String correlationId, String agentType) {
        rolloverIfNeeded();
        globalToday.updateAndGet(v -> v + cost);
        if (correlationId != null) {
            workflowSpend.merge(correlationId, cost, Double::sum);
        }
        if (agentType != null) {
            agentSpend.merge(agentType, cost, Double::sum);
        }
    }

    public double globalToday() {
        rolloverIfNeeded();
        return globalToday.get();
    }

    public double workflow(String correlationId) {
        return correlationId == null ? 0.0 : workflowSpend.getOrDefault(correlationId, 0.0);
    }

    public double agent(String agentType) {
        return agentType == null ? 0.0 : agentSpend.getOrDefault(agentType, 0.0);
    }

    private synchronized void rolloverIfNeeded() {
        LocalDate now = clock.get();
        if (!now.equals(globalDay)) {
            globalDay = now;
            globalToday.set(0.0);
        }
    }
}
