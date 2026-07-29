package com.aiqaos.gateway.webhook;

import com.aiqaos.core.contract.WorkflowResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * WF-2: Webhook Manager for CI & Scheduled Pipeline Triggers.
 */
@Component
public class WebhookManager {

    private final JenkinsWebhookHandler jenkinsHandler;
    private final GithubWebhookHandler  githubHandler;
    private final GitlabWebhookHandler  gitlabHandler;
    private final AzureDevOpsWebhookHandler azureHandler;

    public WebhookManager(JenkinsWebhookHandler jenkinsHandler,
                          GithubWebhookHandler githubHandler,
                          GitlabWebhookHandler gitlabHandler,
                          AzureDevOpsWebhookHandler azureHandler) {
        this.jenkinsHandler = jenkinsHandler;
        this.githubHandler  = githubHandler;
        this.gitlabHandler  = gitlabHandler;
        this.azureHandler   = azureHandler;
    }

    public WorkflowResponse dispatch(String source, Map<String, Object> payload) {
        if (source == null) {
            throw new IllegalArgumentException("Webhook source cannot be null");
        }
        return switch (source.toUpperCase()) {
            case "JENKINS"       -> jenkinsHandler.handle(payload);
            case "GITHUB"        -> githubHandler.handle(payload);
            case "GITLAB"        -> gitlabHandler.handle(payload);
            case "AZURE_DEVOPS"  -> azureHandler.handle(payload);
            default -> throw new IllegalArgumentException("Unknown webhook source: " + source);
        };
    }
}