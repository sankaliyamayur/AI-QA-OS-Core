package com.aiqaos.notification;

import com.aiqaos.integration.plugin.integration.IntegrationRequest;
import com.aiqaos.integration.plugin.integration.IntegrationResponse;
import com.aiqaos.integration.plugin.integration.SlackIntegrationPlugin;
import org.springframework.stereotype.Component;

/**
 * MOD-2: the Slack channel — <b>delegates to the PLG-2 {@link SlackIntegrationPlugin}</b>, realising
 * "the existing SlackPlugin becomes an adapter here". The plugin's transport is simulated today; a
 * real Slack webhook is deferred (FI-MOD2-B).
 */
@Component
public class SlackNotificationSender implements NotificationSender {

    private final SlackIntegrationPlugin slack;

    public SlackNotificationSender(SlackIntegrationPlugin slack) {
        this.slack = slack;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SLACK;
    }

    @Override
    public NotificationResult send(Notification n) {
        String payload = "[" + n.getSeverity() + "] " + n.getSubject() + " — " + n.getBody();
        IntegrationResponse resp = slack.execute(IntegrationRequest.of("notify", payload));
        return resp.isSuccess()
                ? NotificationResult.delivered("slack → " + resp.getMessage())
                : NotificationResult.failed("slack failed: " + resp.getMessage());
    }
}
