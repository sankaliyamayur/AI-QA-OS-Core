package com.aiqaos.integration.event;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.aiqaos.core.event.BaseEvent;
import com.aiqaos.core.event.InProcessEventBus;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * FI-SCALE2-A (ADR-060): a BaseEvent published via the unified {@link PlatformEventBus} entry point
 * reaches BOTH a core {@link com.aiqaos.core.event.EventBus} subscriber AND (via {@link SpringEventBridge})
 * a Spring consumer — so publishers can move to the seam without breaking @EventListener consumers.
 */
class SpringEventBridgeTest {

    static class SampleEvent extends BaseEvent {}

    @Test
    void seamEventReachesBothCoreSubscribersAndSpringConsumers() {
        InProcessEventBus bus = new InProcessEventBus();

        AtomicReference<Object> springReceived = new AtomicReference<>();
        ApplicationEventPublisher springPublisher = springReceived::set; // publishEvent(Object)

        AtomicReference<Object> coreReceived = new AtomicReference<>();
        bus.subscribe(SampleEvent.class, coreReceived::set);

        // Bridge: seam -> Spring.
        new SpringEventBridge(bus, springPublisher).register();

        // Unified entry point publishes onto the seam.
        PlatformEventBus platform = new PlatformEventBus(springPublisher, bus);
        SampleEvent event = new SampleEvent();
        platform.publish(event);

        assertSame(event, coreReceived.get(), "core subscriber receives the seam event");
        assertSame(event, springReceived.get(), "Spring consumer receives it via the bridge");
    }
}
