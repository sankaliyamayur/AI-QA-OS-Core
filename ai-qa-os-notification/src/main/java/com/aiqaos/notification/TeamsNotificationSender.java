package com.aiqaos.notification;

import org.springframework.stereotype.Component;

/**
 * MOD-2: the Microsoft Teams channel (simulated). A real Teams webhook is deferred (FI-MOD2-B).
 */
@Component
public class TeamsNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.TEAMS;
    }

    @Override
    public NotificationResult send(Notification n) {
        return NotificationResult.delivered(
                "[teams] to " + n.getRecipient() + " — " + n.getSubject() + " (simulated)");
    }
}
