package com.aiqaos.gateway.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class GithubWebhookHandler {
    private static final Logger log = LoggerFactory.getLogger(GithubWebhookHandler.class);

    public void handle(Map<String, Object> payload) {
        log.info("GitHub webhook received: event={}", payload.get("event"));
        // TODO: trigger workflow based on push/PR event
    }
}