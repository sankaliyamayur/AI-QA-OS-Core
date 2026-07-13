package com.aiqaos.gateway.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExecutionWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public ExecutionWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendExecutionEvent(String executionId, String eventType, Map<String, Object> payload) {
        String topic = "/topic/execution/" + executionId;
        messagingTemplate.convertAndSend(topic, payload);
    }

    public void sendAgentEvent(String agentId, Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/agent/" + agentId, payload);
    }

    public void sendWorkflowEvent(String workflowId, Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/workflow/" + workflowId, payload);
    }

    public void sendSystemEvent(Map<String, Object> payload) {
        messagingTemplate.convertAndSend("/topic/system", payload);
    }
}