package com.aiqaos.gateway.webhook;

import com.aiqaos.core.contract.WorkflowResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JenkinsWebhookHandler {
    private static final Logger log = LoggerFactory.getLogger(JenkinsWebhookHandler.class);

    private final CiPipelineTriggerHelper triggerHelper;

    public JenkinsWebhookHandler(CiPipelineTriggerHelper triggerHelper) {
        this.triggerHelper = triggerHelper;
    }

    public WorkflowResponse handle(Map<String, Object> payload) {
        String branch = payload != null && payload.get("branch") != null ? payload.get("branch").toString() : "main";
        String commitHash = payload != null && payload.get("commit") != null ? payload.get("commit").toString() : "HEAD";
        String repoName = payload != null && payload.get("jobName") != null ? payload.get("jobName").toString() : "jenkins-job";

        log.info("WF-2: Jenkins Webhook received for job: {} branch: {} commit: {}", repoName, branch, commitHash);
        return triggerHelper.triggerCiWorkflow("JENKINS", branch, commitHash, repoName, "FULL_E2E");
    }
}