package com.aiqaos.gateway.webhook;

import com.aiqaos.core.contract.WorkflowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GitlabWebhookHandler {
    private static final Logger log = LoggerFactory.getLogger(GitlabWebhookHandler.class);

    private final CiPipelineTriggerHelper triggerHelper;

    public GitlabWebhookHandler(CiPipelineTriggerHelper triggerHelper) {
        this.triggerHelper = triggerHelper;
    }

    public WorkflowResponse handle(Map<String, Object> payload) {
        String ref = payload != null && payload.get("ref") != null ? payload.get("ref").toString() : "refs/heads/main";
        String branch = ref.replace("refs/heads/", "");
        String commitHash = payload != null && payload.get("checkout_sha") != null ? payload.get("checkout_sha").toString() : "HEAD";

        String repoName = "gitlab-repo";
        if (payload != null && payload.get("project") instanceof Map<?, ?> projMap) {
            Object nameObj = projMap.get("path_with_namespace");
            if (nameObj != null) repoName = nameObj.toString();
        }

        log.info("WF-2: GitLab Webhook received for repo: {} branch: {} commit: {}", repoName, branch, commitHash);
        return triggerHelper.triggerCiWorkflow("GITLAB", branch, commitHash, repoName, "FULL_E2E");
    }
}