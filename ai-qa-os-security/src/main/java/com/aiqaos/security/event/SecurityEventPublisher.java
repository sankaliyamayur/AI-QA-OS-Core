package com.aiqaos.security.event;

import org.springframework.stereotype.Component;

@Component
public class SecurityEventPublisher {

    public void publish(SecurityEvent event) {
        // Future: publish to application event bus / observability platform
    }
}