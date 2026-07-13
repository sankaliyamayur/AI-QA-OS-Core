package com.aiqaos.gateway.websocket;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class EventStreamController {

    @MessageMapping("/subscribe/execution/{executionId}")
    @SendTo("/topic/execution/{executionId}")
    public Map<String, Object> subscribeToExecution(@DestinationVariable String executionId) {
        return Map.of(
            "type",        "SUBSCRIPTION_CONFIRMED",
            "executionId", executionId,
            "message",     "Subscribed to execution events"
        );
    }
}