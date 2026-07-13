package com.aiqaos.integration.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PlatformEventBus {
    private static final Logger log = LoggerFactory.getLogger(PlatformEventBus.class);
    private final ApplicationEventPublisher publisher;

    public PlatformEventBus(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publishEvent(ApplicationEvent event) {
        log.info("Publishing integration event: {}", event.getClass().getSimpleName());
        publisher.publishEvent(event);
    }
}