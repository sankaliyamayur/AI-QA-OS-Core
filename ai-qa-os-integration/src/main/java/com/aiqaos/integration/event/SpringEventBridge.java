package com.aiqaos.integration.event;

import com.aiqaos.core.event.BaseEvent;
import com.aiqaos.core.event.EventBus;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * FI-SCALE2-A (ADR-060): bridges the {@code core} {@link EventBus} seam to Spring so a {@link BaseEvent}
 * published on the seam ALSO reaches existing {@code @EventListener} consumers. This lets publishers
 * migrate to the seam (e.g. {@code WorkflowEventPublisher}) without breaking Spring-based consumers —
 * the additive half of the FI-SCALE2-A consolidation.
 */
@Component
public class SpringEventBridge {

    private final EventBus eventBus;
    private final ApplicationEventPublisher springPublisher;

    public SpringEventBridge(EventBus eventBus, ApplicationEventPublisher springPublisher) {
        this.eventBus = eventBus;
        this.springPublisher = springPublisher;
    }

    @PostConstruct
    void register() {
        // Forward every seam event to Spring; @EventListener consumers receive it as a payload event.
        eventBus.subscribe(BaseEvent.class, springPublisher::publishEvent);
    }
}
