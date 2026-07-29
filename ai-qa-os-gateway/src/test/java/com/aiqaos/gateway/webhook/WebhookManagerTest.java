package com.aiqaos.gateway.webhook;

import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.gateway.controller.WebhookController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WebhookManagerTest {

    private WebhookManager webhookManager;
    private WebhookController controller;

    @BeforeEach
    void setUp() {
        CiPipelineTriggerHelper helper = new CiPipelineTriggerHelper(new TestingObjectProvider<>(null));
        GithubWebhookHandler github = new GithubWebhookHandler(helper);
        GitlabWebhookHandler gitlab = new GitlabWebhookHandler(helper);
        JenkinsWebhookHandler jenkins = new JenkinsWebhookHandler(helper);
        AzureDevOpsWebhookHandler azure = new AzureDevOpsWebhookHandler(helper);

        webhookManager = new WebhookManager(jenkins, github, gitlab, azure);
        controller = new WebhookController(webhookManager);
    }

    @Test
    @DisplayName("WF-2: Should dispatch GitHub CI push webhook and return success workflow response")
    void testGithubWebhookDispatch() {
        Map<String, Object> payload = Map.of(
                "ref", "refs/heads/feature/login",
                "after", "abc12345",
                "repository", Map.of("full_name", "acme/qa-repo")
        );

        WorkflowResponse response = webhookManager.dispatch("GITHUB", payload);
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("GITHUB", response.getOutputs().get("ciSource"));
        assertEquals("feature/login", response.getOutputs().get("branch"));
    }

    @Test
    @DisplayName("WF-2: Should dispatch GitLab CI push webhook")
    void testGitlabWebhookDispatch() {
        Map<String, Object> payload = Map.of(
                "ref", "refs/heads/main",
                "checkout_sha", "def67890",
                "project", Map.of("path_with_namespace", "acme/gitlab-repo")
        );

        WorkflowResponse response = webhookManager.dispatch("GITLAB", payload);
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("GITLAB", response.getOutputs().get("ciSource"));
        assertEquals("main", response.getOutputs().get("branch"));
    }

    @Test
    @DisplayName("WF-2: Controller POST /api/v1/webhooks/{source} returns 200 OK")
    void testWebhookControllerEndpoint() {
        ResponseEntity<WorkflowResponse> response = controller.receiveWebhook("GITHUB", Map.of("ref", "refs/heads/dev"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("dev", response.getBody().getOutputs().get("branch"));
    }

    @Test
    @DisplayName("WF-2: Controller POST /api/v1/webhooks/schedule returns 200 OK")
    void testScheduleEndpoint() {
        ResponseEntity<WorkflowResponse> response = controller.triggerScheduledRun(Map.of("schedule", "cron"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getStatus());
    }

    private static class TestingObjectProvider<T> implements ObjectProvider<T> {
        private final T instance;
        TestingObjectProvider(T instance) { this.instance = instance; }
        @Override public T getObject() { return instance; }
        @Override public T getObject(Object... args) { return instance; }
        @Override public T getIfAvailable() { return instance; }
        @Override public T getIfUnique() { return instance; }
    }
}
