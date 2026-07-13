package com.aiqaos.gateway.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class JenkinsWebhookHandler {
    private static final Logger log = LoggerFactory.getLogger(JenkinsWebhookHandler.class);

    public void handle(Map<String, Object> payload) {
        log.info("Jenkins webhook received: {}", payload);
        // TODO: trigger regression workflow on build completion
    }
}