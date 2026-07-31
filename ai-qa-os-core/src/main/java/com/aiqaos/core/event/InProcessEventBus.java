package com.aiqaos.core.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SCALE-2 (ADR-060): synchronous in-process {@link EventBus} default — a thread-safe type→handlers
 * registry. {@code publish} walks the event's class hierarchy so a {@code Consumer<BaseEvent>} sees
 * every event while a {@code Consumer<WorkflowEvent>} sees only workflow events. A throwing handler is
 * isolated (logged) so it cannot stop the others. No infrastructure — deterministic and unit-testable.
 * A distributed bus (Kafka) is a deferred binding of the same interface.
 */
@Component
public class InProcessEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(InProcessEventBus.class);

    private final Map<Class<?>, List<Consumer<BaseEvent>>> handlers = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BaseEvent> void subscribe(Class<T> type, Consumer<T> handler) {
        handlers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>())
                .add((Consumer<BaseEvent>) handler);
    }

    @Override
    public void publish(BaseEvent event) {
        if (event == null) {
            return;
        }
        // Dispatch to handlers registered for the event's concrete type or any supertype up to BaseEvent.
        for (Class<?> c = event.getClass(); c != null && BaseEvent.class.isAssignableFrom(c); c = c.getSuperclass()) {
            List<Consumer<BaseEvent>> list = handlers.get(c);
            if (list == null) {
                continue;
            }
            for (Consumer<BaseEvent> handler : list) {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    log.warn("Event handler failed for {}: {}", event.getClass().getSimpleName(), e.toString());
                }
            }
        }
    }
}
