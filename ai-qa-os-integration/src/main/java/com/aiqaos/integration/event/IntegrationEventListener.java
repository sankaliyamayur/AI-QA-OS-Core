package com.aiqaos.integration.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class IntegrationEventListener {
    private static final Logger log = LoggerFactory.getLogger(IntegrationEventListener.class);

    @EventListener
    public void handleEvent(ApplicationEvent event) {
        log.info("EventListener received: {}", event.getClass().getSimpleName());
    }
}