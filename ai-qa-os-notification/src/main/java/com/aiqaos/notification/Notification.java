package com.aiqaos.notification;

/**
 * MOD-2: an outbound message routed through the single egress point — its target {@link #channel},
 * {@code recipient}, {@code subject}, {@code body}, and {@link NotificationSeverity}.
 */
public final class Notification {

    private final NotificationChannel channel;
    private final String recipient;
    private final String subject;
    private final String body;
    private final NotificationSeverity severity;

    public Notification(NotificationChannel channel, String recipient, String subject, String body,
                        NotificationSeverity severity) {
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.severity = severity != null ? severity : NotificationSeverity.INFO;
    }

    public static Notification of(NotificationChannel channel, String recipient, String subject, String body) {
        return new Notification(channel, recipient, subject, body, NotificationSeverity.INFO);
    }

    public NotificationChannel getChannel() { return channel; }
    public String getRecipient() { return recipient; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public NotificationSeverity getSeverity() { return severity; }
}
