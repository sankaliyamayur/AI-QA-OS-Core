package com.aiqaos.core.event;

import java.util.function.Consumer;

/**
 * SCALE-2 (ADR-060): the inter-service coordination seam. Any module depending on {@code core} can
 * publish and subscribe to {@link BaseEvent}s without a dependency on a higher-level module. The
 * in-process {@link InProcessEventBus} is the validatable default; a distributed (Kafka) binding is a
 * deferred drop-in of this same interface — the container is provisioned (ADR-053), built when a real
 * cross-service consumer exists.
 */
public interface EventBus {

    /** Publish an event to every handler registered for its type or any supertype up to {@link BaseEvent}. */
    void publish(BaseEvent event);

    /** Register a handler for events of {@code type} (or its subtypes). */
    <T extends BaseEvent> void subscribe(Class<T> type, Consumer<T> handler);
}
