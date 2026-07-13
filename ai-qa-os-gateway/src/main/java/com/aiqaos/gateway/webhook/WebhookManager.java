package com.aiqaos.gateway.webhook;

import org.springframework.stereotype.Component;

import java.util.Map;

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

    public void dispatch(String source, Map<String, Object> payload) {
        switch (source.toUpperCase()) {
            case "JENKINS"       -> jenkinsHandler.handle(payload);
            case "GITHUB"        -> githubHandler.handle(payload);
            case "GITLAB"        -> gitlabHandler.handle(payload);
            case "AZURE_DEVOPS"  -> azureHandler.handle(payload);
            default -> throw new IllegalArgumentException("Unknown webhook source: " + source);
        }
    }
}