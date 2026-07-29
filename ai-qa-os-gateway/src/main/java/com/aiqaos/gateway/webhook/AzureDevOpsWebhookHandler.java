package com.aiqaos.gateway.webhook;

import com.aiqaos.core.contract.WorkflowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AzureDevOpsWebhookHandler {
    private static final Logger log = LoggerFactory.getLogger(AzureDevOpsWebhookHandler.class);

    private final CiPipelineTriggerHelper triggerHelper;

    public AzureDevOpsWebhookHandler(CiPipelineTriggerHelper triggerHelper) {
        this.triggerHelper = triggerHelper;
    }

    public WorkflowResponse handle(Map<String, Object> payload) {
        String branch = payload != null && payload.get("branch") != null ? payload.get("branch").toString() : "main";
        String commitHash = payload != null && payload.get("commitId") != null ? payload.get("commitId").toString() : "HEAD";
        String repoName = payload != null && payload.get("repository") != null ? payload.get("repository").toString() : "azure-repo";

        log.info("WF-2: Azure DevOps Webhook received for repo: {} branch: {} commit: {}", repoName, branch, commitHash);
        return triggerHelper.triggerCiWorkflow("AZURE_DEVOPS", branch, commitHash, repoName, "FULL_E2E");
    }
}