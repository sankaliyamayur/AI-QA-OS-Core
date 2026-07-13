package com.aiqaos.gateway.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class GatewayEventPublisher {

    private final ApplicationEventPublisher publisher;

    public GatewayEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(GatewayEvent event) {
        publisher.publishEvent(event);
    }

    public void publishCompleted(String correlationId, String userId, String endpoint,
                                 long durationMs, int statusCode) {
        GatewayEvent event = new GatewayEvent();
        event.setType(GatewayEventType.REQUEST_COMPLETED);
        event.setCorrelationId(correlationId);
        event.setUserId(userId);
        event.setEndpoint(endpoint);
        event.setDurationMs(durationMs);
        event.setStatusCode(statusCode);
        publish(event);
    }

    public void publishFailed(String correlationId, String endpoint, int statusCode) {
        GatewayEvent event = new GatewayEvent();
        event.setType(GatewayEventType.REQUEST_FAILED);
        event.setCorrelationId(correlationId);
        event.setEndpoint(endpoint);
        event.setStatusCode(statusCode);
        publish(event);
    }
}