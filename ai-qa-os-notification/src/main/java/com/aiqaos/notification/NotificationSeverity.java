package com.aiqaos.notification;

/**
 * MOD-2: the urgency of a notification — for future routing/filtering (e.g. only CRITICAL to on-call).
 */
public enum NotificationSeverity {
    INFO,
    WARNING,
    CRITICAL
}
