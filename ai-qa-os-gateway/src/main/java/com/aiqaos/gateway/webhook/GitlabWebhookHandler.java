package com.aiqaos.gateway.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class GitlabWebhookHandler {
    private static final Logger log = LoggerFactory.getLogger(GitlabWebhookHandler.class);

    public void handle(Map<String, Object> payload) {
        log.info("GitLab webhook received: {}", payload);
    }
}