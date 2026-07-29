package com.aiqaos.notification;

/**
 * MOD-2: the outbound channels the notification egress point can route to.
 */
public enum NotificationChannel {
    SLACK,
    EMAIL,
    WEBHOOK,
    TEAMS
}
