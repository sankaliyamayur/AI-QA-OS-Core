package com.aiqaos.gateway.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Spring Boot 4 / Framework 7 note: the {@code (Object)} casts below are required, not decorative.
 * The payload is a {@code Map<String, Object>}, which in Framework 7 matches both
 * {@code convertAndSend(D destination, T payload)} and the headers overload
 * {@code convertAndSend(D, T, Map<String, Object>)}, so the call is ambiguous without one. The cast
 * pins it to the two-argument form — the same method that was resolved under Framework 6.
 */
@Component
public class ExecutionWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public ExecutionWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendExecutionEvent(String executionId, String eventType, Map<String, Object> payload) {
        String topic = "/topic/execution/" + executionId;
        messagingTemplate.convertAndSend(topic, (Object) payload);
    }

    public void sendAgentEvent(String agentId, Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/agent/" + agentId, (Object) payload);
    }

    public void sendWorkflowEvent(String workflowId, Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/workflow/" + workflowId, (Object) payload);
    }

    public void sendSystemEvent(Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/system", (Object) payload);
    }
}