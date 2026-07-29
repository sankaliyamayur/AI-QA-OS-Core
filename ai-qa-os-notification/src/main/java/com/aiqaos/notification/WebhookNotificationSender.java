package com.aiqaos.notification;

import org.springframework.stereotype.Component;

/**
 * MOD-2: the generic webhook channel (simulated). A real HTTP POST transport is deferred (FI-MOD2-B).
 */
@Component
public class WebhookNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.WEBHOOK;
    }

    @Override
    public NotificationResult send(Notification n) {
        return NotificationResult.delivered(
                "[webhook] POST " + n.getRecipient() + " — " + n.getSubject() + " (simulated)");
    }
}
