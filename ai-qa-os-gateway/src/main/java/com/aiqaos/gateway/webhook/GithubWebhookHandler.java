package com.aiqaos.gateway.webhook;

import com.aiqaos.core.contract.WorkflowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GithubWebhookHandler {
    private static final Logger log = LoggerFactory.getLogger(GithubWebhookHandler.class);

    private final CiPipelineTriggerHelper triggerHelper;

    public GithubWebhookHandler(CiPipelineTriggerHelper triggerHelper) {
        this.triggerHelper = triggerHelper;
    }

    public WorkflowResponse handle(Map<String, Object> payload) {
        String ref = payload != null && payload.get("ref") != null ? payload.get("ref").toString() : "refs/heads/main";
        String branch = ref.replace("refs/heads/", "");
        String commitHash = payload != null && payload.get("after") != null ? payload.get("after").toString() : "HEAD";

        String repoName = "unknown-repo";
        if (payload != null && payload.get("repository") instanceof Map<?, ?> repoMap) {
            Object nameObj = repoMap.get("full_name");
            if (nameObj != null) repoName = nameObj.toString();
        }

        log.info("WF-2: GitHub Webhook received for repo: {} branch: {} commit: {}", repoName, branch, commitHash);
        return triggerHelper.triggerCiWorkflow("GITHUB", branch, commitHash, repoName, "FULL_E2E");
    }
}