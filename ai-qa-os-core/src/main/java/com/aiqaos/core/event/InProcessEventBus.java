package com.aiqaos.core.event;

import java.util.function.Consumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * SCALE-2 (ADR-060): synchronous in-process {@link EventBus} default — delegates to the shared
 * {@link EventDispatch} registry. Deterministic, unit-tested, no infrastructure.
 *
 * <p>The default transport (ADR-064): active unless {@code aiqaos.events.transport} is set to something
 * other than {@code in-process} (e.g. {@code kafka}), in which case the distributed {@code KafkaEventBus}
 * takes over. {@code matchIfMissing=true} keeps in-process the zero-config default.
 */
@Component
@ConditionalOnProperty(name = "aiqaos.events.transport", havingValue = "in-process", matchIfMissing = true)
public class InProcessEventBus implements EventBus {

    private final EventDispatch dispatch = new EventDispatch();

    @Override
    public <T extends BaseEvent> void subscribe(Class<T> type, Consumer<T> handler) {
        dispatch.subscribe(type, handler);
    }

    @Override
    public void publish(BaseEvent event) {
        dispatch.dispatch(event);
    }
}
