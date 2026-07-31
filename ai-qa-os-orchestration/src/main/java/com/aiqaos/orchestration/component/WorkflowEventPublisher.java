package com.aiqaos.orchestration.component;

import com.aiqaos.core.event.BaseEvent;
import com.aiqaos.core.event.EventBus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * FI-SCALE2-A (ADR-060): the representative publisher migrated onto the {@code core} {@link EventBus}
 * seam. A {@link BaseEvent} is published through the seam (and still reaches Spring {@code @EventListener}
 * consumers via {@code SpringEventBridge}); any non-core event falls back to the legacy Spring path
 * during migration. Constructor injection (replaces the prior {@code @Autowired} field).
 */
@Component
public class WorkflowEventPublisher {

    private final EventBus eventBus;
    private final ApplicationEventPublisher eventPublisher;

    public WorkflowEventPublisher(EventBus eventBus, ApplicationEventPublisher eventPublisher) {
        this.eventBus = eventBus;
        this.eventPublisher = eventPublisher;
    }

    public void publish(Object event) {
        if (event instanceof BaseEvent baseEvent) {
            eventBus.publish(baseEvent);
        } else {
            eventPublisher.publishEvent(event);
        }
    }
}