package com.aiqaos.notification;

import org.springframework.stereotype.Component;

/**
 * MOD-2: the email channel (simulated). A real SMTP transport is deferred (FI-MOD2-B) under the SEC-2
 * credential model.
 */
@Component
public class EmailNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public NotificationResult send(Notification n) {
        return NotificationResult.delivered(
                "[email] to " + n.getRecipient() + " — " + n.getSubject() + " (simulated)");
    }
}
