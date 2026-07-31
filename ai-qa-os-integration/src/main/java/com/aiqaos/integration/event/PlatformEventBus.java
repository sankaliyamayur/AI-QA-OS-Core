package com.aiqaos.integration.event;

import com.aiqaos.core.event.BaseEvent;
import com.aiqaos.core.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * FI-SCALE2-A (ADR-060): the unified platform event entry point. {@link #publish(BaseEvent)} routes
 * coordination events through the canonical {@code core} {@link EventBus} seam (SCALE-2) — from where
 * {@link SpringEventBridge} also forwards them to existing {@code @EventListener} consumers, so nothing
 * breaks during migration. {@link #publishEvent(ApplicationEvent)} remains for the legacy Spring path.
 */
@Component
public class PlatformEventBus {
    private static final Logger log = LoggerFactory.getLogger(PlatformEventBus.class);
    private final ApplicationEventPublisher publisher;
    private final EventBus eventBus;

    public PlatformEventBus(ApplicationEventPublisher publisher, EventBus eventBus) {
        this.publisher = publisher;
        this.eventBus = eventBus;
    }

    /** Canonical path: publish a core event through the {@link EventBus} seam (SCALE-2). */
    public void publish(BaseEvent event) {
        log.info("Publishing platform event via seam: {}", event.getClass().getSimpleName());
        eventBus.publish(event);
    }

    /** Legacy Spring path, retained during the FI-SCALE2-A migration. */
    public void publishEvent(ApplicationEvent event) {
        log.info("Publishing integration event: {}", event.getClass().getSimpleName());
        publisher.publishEvent(event);
    }
}