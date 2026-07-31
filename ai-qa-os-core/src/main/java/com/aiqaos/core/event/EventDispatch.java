package com.aiqaos.core.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SCALE-2 (ADR-060/064): the shared local subscriber registry + synchronous type-hierarchy dispatch,
 * reused by both {@link InProcessEventBus} and the distributed {@code KafkaEventBus}. {@code dispatch}
 * walks the event's class hierarchy so a {@code Consumer<BaseEvent>} sees every event while a typed
 * consumer sees only its type; a throwing handler is isolated (logged) so it cannot stop the others.
 */
final class EventDispatch {

    private static final Logger log = LoggerFactory.getLogger(EventDispatch.class);

    private final Map<Class<?>, List<Consumer<BaseEvent>>> handlers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    <T extends BaseEvent> void subscribe(Class<T> type, Consumer<T> handler) {
        handlers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>())
                .add((Consumer<BaseEvent>) handler);
    }

    void dispatch(BaseEvent event) {
        if (event == null) {
            return;
        }
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
