package com.aiqaos.integration.plugin.integration;

import org.springframework.stereotype.Component;

/**
 * PLG-2: Microsoft Teams chat integration (new). Simulated action surface; a real Teams webhook
 * client is a deferred follow-up (FI-PLG2-A).
 */
@Component
public class TeamsIntegrationPlugin extends AbstractIntegrationPlugin {

    public TeamsIntegrationPlugin() {
        super(manifest("teams", caps("chat.notify", "chat.card")));
    }

    @Override
    public IntegrationCategory category() {
        return IntegrationCategory.CHAT;
    }

    @Override
    public IntegrationResponse execute(IntegrationRequest request) {
        String action = request != null ? request.getAction() : "noop";
        String payload = request != null ? request.getPayload() : "";
        return IntegrationResponse.ok("[teams] " + action + " → simulated: " + payload);
    }
}
