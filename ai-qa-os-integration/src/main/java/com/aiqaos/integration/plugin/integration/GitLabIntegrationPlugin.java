package com.aiqaos.integration.plugin.integration;

import org.springframework.stereotype.Component;

/**
 * PLG-2: GitLab SCM integration (new). Simulated action surface; a real GitLab REST client is a
 * deferred follow-up (FI-PLG2-A) under the SEC-2 credential model.
 */
@Component
public class GitLabIntegrationPlugin extends AbstractIntegrationPlugin {

    public GitLabIntegrationPlugin() {
        super(manifest("gitlab", caps("scm.commit", "scm.issue", "ci.trigger")));
    }

    @Override
    public IntegrationCategory category() {
        return IntegrationCategory.SCM;
    }

    @Override
    public IntegrationResponse execute(IntegrationRequest request) {
        String action = request != null ? request.getAction() : "noop";
        String payload = request != null ? request.getPayload() : "";
        return IntegrationResponse.ok("[gitlab] " + action + " → simulated: " + payload);
    }
}
