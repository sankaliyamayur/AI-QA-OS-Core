package com.aiqaos.integration.plugin.integration;

import org.springframework.stereotype.Component;

/**
 * PLG-2: Jenkins CI integration (new). Simulated action surface; a real Jenkins client is a deferred
 * follow-up (FI-PLG2-A).
 */
@Component
public class JenkinsIntegrationPlugin extends AbstractIntegrationPlugin {

    public JenkinsIntegrationPlugin() {
        super(manifest("jenkins", caps("ci.trigger", "ci.status")));
    }

    @Override
    public IntegrationCategory category() {
        return IntegrationCategory.CI;
    }

    @Override
    public IntegrationResponse execute(IntegrationRequest request) {
        String action = request != null ? request.getAction() : "noop";
        String payload = request != null ? request.getPayload() : "";
        return IntegrationResponse.ok("[jenkins] " + action + " → simulated: " + payload);
    }
}
