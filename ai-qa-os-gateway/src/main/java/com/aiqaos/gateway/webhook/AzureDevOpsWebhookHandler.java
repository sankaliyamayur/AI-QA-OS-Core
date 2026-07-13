package com.aiqaos.gateway.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class AzureDevOpsWebhookHandler {
    private static final Logger log = LoggerFactory.getLogger(AzureDevOpsWebhookHandler.class);

    public void handle(Map<String, Object> payload) {
        log.info("Azure DevOps webhook received: {}", payload);
    }
}