package com.aiqaos.integration.plugin.integration;

import com.aiqaos.orchestration.plugin.JiraPlugin;
import org.springframework.stereotype.Component;

/**
 * PLG-2: Jira as a first-class integration plugin — migrates the existing {@link JiraPlugin} onto the
 * PLG-1 contract by delegation.
 */
@Component
public class JiraIntegrationPlugin extends AbstractIntegrationPlugin {

    private final JiraPlugin delegate;

    public JiraIntegrationPlugin(JiraPlugin delegate) {
        super(manifest("jira", caps("alm.issue", "alm.comment")));
        this.delegate = delegate;
    }

    @Override
    public IntegrationCategory category() {
        return IntegrationCategory.ALM;
    }

    @Override
    public IntegrationResponse execute(IntegrationRequest request) {
        String payload = request != null ? request.getPayload() : "";
        return IntegrationResponse.ok(delegate.execute(payload));
    }
}
