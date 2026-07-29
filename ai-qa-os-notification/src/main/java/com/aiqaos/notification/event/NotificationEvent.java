package com.aiqaos.notification.event;

import com.aiqaos.notification.NotificationChannel;

/**
 * ENT-2: a platform event to be turned into a stakeholder notification — its {@link NotificationEventType},
 * the thing it concerns ({@code subjectRef}, e.g. a workflow id), a human {@code summary}, the
 * {@code recipient}, and an optional {@code channel} (falls back to the configured default when null).
 */
public final class NotificationEvent {

    private final NotificationEventType type;
    private final String subjectRef;
    private final String summary;
    private final String recipient;
    private final NotificationChannel channel; // nullable → default

    public NotificationEvent(NotificationEventType type, String subjectRef, String summary,
                             String recipient, NotificationChannel channel) {
        this.type = type;
        this.subjectRef = subjectRef;
        this.summary = summary;
        this.recipient = recipient;
        this.channel = channel;
    }

    public static NotificationEvent of(NotificationEventType type, String subjectRef, String summary,
                                       String recipient) {
        return new NotificationEvent(type, subjectRef, summary, recipient, null);
    }

    public NotificationEventType getType() { return type; }
    public String getSubjectRef() { return subjectRef; }
    public String getSummary() { return summary; }
    public String getRecipient() { return recipient; }
    public NotificationChannel getChannel() { return channel; }
}
