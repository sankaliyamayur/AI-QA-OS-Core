package com.aiqaos.gateway.controller;

import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.gateway.webhook.WebhookManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * WF-2: Webhook Controller for CI/CD & Scheduled Runs.
 */
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final WebhookManager webhookManager;

    public WebhookController(WebhookManager webhookManager) {
        this.webhookManager = webhookManager;
    }

    @PostMapping("/{source}")
    public ResponseEntity<WorkflowResponse> receiveWebhook(@PathVariable String source,
                                                          @RequestBody(required = false) Map<String, Object> payload) {
        Map<String, Object> body = payload != null ? payload : Map.of();
        WorkflowResponse response = webhookManager.dispatch(source, body);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/schedule")
    public ResponseEntity<WorkflowResponse> triggerScheduledRun(@RequestBody(required = false) Map<String, Object> payload) {
        Map<String, Object> body = payload != null ? payload : Map.of();
        WorkflowResponse response = webhookManager.dispatch("GITHUB", body);
        return ResponseEntity.ok(response);
    }
}