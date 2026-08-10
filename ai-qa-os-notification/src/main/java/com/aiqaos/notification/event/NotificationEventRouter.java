package com.aiqaos.notification.event;

import com.aiqaos.notification.Notification;
import com.aiqaos.notification.NotificationChannel;
import com.aiqaos.notification.NotificationResult;
import com.aiqaos.notification.NotificationService;
import com.aiqaos.notification.NotificationSeverity;
import org.springframework.stereotype.Service;

/**
 * ENT-2: the centralised entry point for stakeholder notifications. Turns a {@link NotificationEvent}
 * (run complete / failed / approval needed) into a templated, severity-ranked {@link Notification}
 * and dispatches it through MOD-2's {@link NotificationService} egress point — so all run/approval
 * comms flow through one governed path. This is the piece that lets AI-2's approval reach a human.
 */
@Service
public class NotificationEventRouter {

    private final NotificationService notificationService;
    private final NotificationEventProperties properties;

    public NotificationEventRouter(NotificationService notificationService,
                                   NotificationEventProperties properties) {
        this.notificationService = notificationService;
        this.properties = properties;
    }

    public NotificationResult route(NotificationEvent event) {
        if (event == null || event.getType() == null) {
            return NotificationResult.failed("notification event or type is null");
        }
        NotificationChannel channel = event.getChannel() != null
                ? event.getChannel()
                : properties.getDefaultChannel();

        Notification notification = new Notification(channel, event.getRecipient(),
                subject(event), body(event), severity(event.getType()));
        return notificationService.notify(notification);
    }

    private NotificationSeverity severity(NotificationEventType type) {
        return switch (type) {
            case RUN_FAILURE -> NotificationSeverity.CRITICAL;
            // FI-PE3-D: a quality decline needs attention but nothing is broken — WARNING, not CRITICAL,
            // so prompt regressions can't drown out actual run failures.
            case APPROVAL_REQUEST, PROMPT_REGRESSION -> NotificationSeverity.WARNING;
            case RUN_COMPLETE -> NotificationSeverity.INFO;
        };
    }

    private String subject(NotificationEvent e) {
        return switch (e.getType()) {
            case RUN_COMPLETE -> "Run complete: " + e.getSubjectRef();
            case RUN_FAILURE -> "Run FAILED: " + e.getSubjectRef();
            case APPROVAL_REQUEST -> "Approval required: " + e.getSubjectRef();
            case PROMPT_REGRESSION -> "Prompt quality regression: " + e.getSubjectRef();
        };
    }

    private String body(NotificationEvent e) {
        return switch (e.getType()) {
            case APPROVAL_REQUEST -> "An action needs your approval: " + e.getSummary();
            default -> e.getSummary();
        };
    }
}
