package com.aiqaos.integration.plugin.integration;

import com.aiqaos.orchestration.plugin.GithubPlugin;
import org.springframework.stereotype.Component;

/**
 * PLG-2: GitHub as a first-class integration plugin — migrates the existing {@link GithubPlugin}
 * ({@code PluginStep}) onto the PLG-1 contract by delegation (its logic is reused, not rewritten).
 */
@Component
public class GitHubIntegrationPlugin extends AbstractIntegrationPlugin {

    private final GithubPlugin delegate;

    public GitHubIntegrationPlugin(GithubPlugin delegate) {
        super(manifest("github", caps("scm.commit", "scm.issue")));
        this.delegate = delegate;
    }

    @Override
    public IntegrationCategory category() {
        return IntegrationCategory.SCM;
    }

    @Override
    public IntegrationResponse execute(IntegrationRequest request) {
        String payload = request != null ? request.getPayload() : "";
        return IntegrationResponse.ok(delegate.execute(payload));
    }
}
