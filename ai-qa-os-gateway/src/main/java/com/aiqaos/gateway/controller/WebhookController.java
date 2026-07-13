package com.aiqaos.gateway.controller;

import com.aiqaos.gateway.webhook.WebhookManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final WebhookManager webhookManager;

    public WebhookController(WebhookManager webhookManager) {
        this.webhookManager = webhookManager;
    }

    @PostMapping("/{source}")
    public ResponseEntity<String> receive(@PathVariable String source,
                                          @RequestBody Map<String, Object> payload) {
        webhookManager.dispatch(source, payload);
        return ResponseEntity.ok("Webhook received");
    }
}