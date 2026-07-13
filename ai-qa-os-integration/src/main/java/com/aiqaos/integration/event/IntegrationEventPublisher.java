package com.aiqaos.integration.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

@Component
public class IntegrationEventPublisher {

    private final PlatformEventBus eventBus;

    public IntegrationEventPublisher(PlatformEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void publish(ApplicationEvent event) {
        eventBus.publishEvent(event);
    }
}