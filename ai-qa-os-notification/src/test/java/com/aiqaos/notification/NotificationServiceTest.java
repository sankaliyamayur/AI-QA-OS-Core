package com.aiqaos.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.integration.plugin.integration.SlackIntegrationPlugin;
import com.aiqaos.orchestration.plugin.SlackPlugin;
import java.util.List;
import org.junit.jupiter.api.Test;

/** MOD-2: unit tests for the notification egress point — route-by-channel + graceful no-sender. */
class NotificationServiceTest {

    private NotificationService fullService() {
        return new NotificationService(List.of(
                new SlackNotificationSender(new SlackIntegrationPlugin(new SlackPlugin())),
                new EmailNotificationSender(),
                new WebhookNotificationSender(),
                new TeamsNotificationSender()));
    }

    @Test
    void routesEmailToEmailSender() {
        NotificationResult r = fullService().notify(
                Notification.of(NotificationChannel.EMAIL, "qa@acme.test", "Run failed", "3 tests red"));
        assertThat(r.isDelivered()).isTrue();
        assertThat(r.getMessage()).contains("[email]").contains("qa@acme.test");
    }

    @Test
    void routesSlackThroughThePluginAdapter() {
        NotificationResult r = fullService().notify(
                Notification.of(NotificationChannel.SLACK, "#qa", "Run failed", "3 tests red"));
        assertThat(r.isDelivered()).isTrue();
        assertThat(r.getMessage()).containsIgnoringCase("slack");
    }

    @Test
    void routesWebhookAndTeams() {
        NotificationService svc = fullService();
        assertThat(svc.notify(Notification.of(NotificationChannel.WEBHOOK, "https://h", "s", "b")).isDelivered()).isTrue();
        assertThat(svc.notify(Notification.of(NotificationChannel.TEAMS, "#qa", "s", "b")).isDelivered()).isTrue();
    }

    @Test
    void unconfiguredChannelFailsGracefully() {
        NotificationService svc = new NotificationService(List.of(new EmailNotificationSender()));
        NotificationResult r = svc.notify(Notification.of(NotificationChannel.SLACK, "#qa", "s", "b"));
        assertThat(r.isDelivered()).isFalse();
        assertThat(r.getMessage()).contains("no sender configured");
        assertThat(svc.supportedChannels()).containsExactly(NotificationChannel.EMAIL);
    }

    @Test
    void nullNotificationIsHandled() {
        assertThat(fullService().notify(null).isDelivered()).isFalse();
    }
}
