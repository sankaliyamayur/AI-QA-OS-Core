package com.aiqaos.integration.plugin.integration;

import org.springframework.stereotype.Component;

/**
 * PLG-2: Azure DevOps CI integration (new). Simulated action surface; a real Azure DevOps REST client
 * is a deferred follow-up (FI-PLG2-A).
 */
@Component
public class AzureDevOpsIntegrationPlugin extends AbstractIntegrationPlugin {

    public AzureDevOpsIntegrationPlugin() {
        super(manifest("azure-devops", caps("ci.trigger", "ci.status", "alm.workitem")));
    }

    @Override
    public IntegrationCategory category() {
        return IntegrationCategory.CI;
    }

    @Override
    public IntegrationResponse execute(IntegrationRequest request) {
        String action = request != null ? request.getAction() : "noop";
        String payload = request != null ? request.getPayload() : "";
        return IntegrationResponse.ok("[azure-devops] " + action + " → simulated: " + payload);
    }
}
