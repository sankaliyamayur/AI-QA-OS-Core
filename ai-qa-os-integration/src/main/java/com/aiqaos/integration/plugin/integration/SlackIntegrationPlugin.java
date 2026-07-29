package com.aiqaos.integration.plugin.integration;

import com.aiqaos.orchestration.plugin.SlackPlugin;
import org.springframework.stereotype.Component;

/**
 * PLG-2: Slack as a first-class integration plugin — migrates the existing {@link SlackPlugin} onto
 * the PLG-1 contract by delegation.
 */
@Component
public class SlackIntegrationPlugin extends AbstractIntegrationPlugin {

    private final SlackPlugin delegate;

    public SlackIntegrationPlugin(SlackPlugin delegate) {
        super(manifest("slack", caps("chat.notify")));
        this.delegate = delegate;
    }

    @Override
    public IntegrationCategory category() {
        return IntegrationCategory.CHAT;
    }

    @Override
    public IntegrationResponse execute(IntegrationRequest request) {
        String payload = request != null ? request.getPayload() : "";
        return IntegrationResponse.ok(delegate.execute(payload));
    }
}
