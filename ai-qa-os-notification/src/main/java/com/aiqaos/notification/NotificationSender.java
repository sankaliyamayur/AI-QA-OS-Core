package com.aiqaos.notification;

/**
 * MOD-2 SPI: sends a notification on one {@link NotificationChannel}. The {@link NotificationService}
 * dispatcher selects the sender whose {@link #channel()} matches. Real transports (SMTP/Slack/HTTP)
 * are deferred behind this seam (FI-MOD2-B).
 */
public interface NotificationSender {

    NotificationChannel channel();

    NotificationResult send(Notification notification);
}
