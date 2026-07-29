package com.aiqaos.notification.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.notification.Notification;
import com.aiqaos.notification.NotificationChannel;
import com.aiqaos.notification.NotificationResult;
import com.aiqaos.notification.NotificationSender;
import com.aiqaos.notification.NotificationService;
import com.aiqaos.notification.NotificationSeverity;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** ENT-2: unit tests for event → templated notification routing over MOD-2. No Mockito. */
class NotificationEventRouterTest {

    /** Records the last notification it was handed. */
    private static final class RecordingSender implements NotificationSender {
        final NotificationChannel ch;
        Notification last;
        RecordingSender(NotificationChannel ch) { this.ch = ch; }
        public NotificationChannel channel() { return ch; }
        public NotificationResult send(Notification n) { this.last = n; return NotificationResult.delivered("ok"); }
    }

    private RecordingSender slack;
    private RecordingSender email;
    private NotificationEventRouter router;

    @BeforeEach
    void setup() {
        slack = new RecordingSender(NotificationChannel.SLACK);
        email = new RecordingSender(NotificationChannel.EMAIL);
        NotificationService service = new NotificationService(List.of(slack, email));
        router = new NotificationEventRouter(service, new NotificationEventProperties()); // default SLACK
    }

    @Test
    void runFailureIsCriticalAndDefaultChannel() {
        NotificationResult r = router.route(
                NotificationEvent.of(NotificationEventType.RUN_FAILURE, "WF-1", "3 tests red", "#qa"));
        assertThat(r.isDelivered()).isTrue();
        assertThat(slack.last.getSeverity()).isEqualTo(NotificationSeverity.CRITICAL);
        assertThat(slack.last.getSubject()).contains("FAILED").contains("WF-1");
    }

    @Test
    void approvalRequestIsWarningWithApprovalBody() {
        router.route(NotificationEvent.of(NotificationEventType.APPROVAL_REQUEST, "WF-2",
                "locator heal needs sign-off", "#qa"));
        assertThat(slack.last.getSeverity()).isEqualTo(NotificationSeverity.WARNING);
        assertThat(slack.last.getSubject()).contains("Approval required");
        assertThat(slack.last.getBody()).containsIgnoringCase("approval");
    }

    @Test
    void runCompleteIsInfo() {
        router.route(NotificationEvent.of(NotificationEventType.RUN_COMPLETE, "WF-3", "all green", "#qa"));
        assertThat(slack.last.getSeverity()).isEqualTo(NotificationSeverity.INFO);
        assertThat(slack.last.getSubject()).contains("Run complete");
    }

    @Test
    void eventChannelOverridesTheDefault() {
        router.route(new NotificationEvent(NotificationEventType.RUN_COMPLETE, "WF-4", "done",
                "qa@acme.test", NotificationChannel.EMAIL));
        assertThat(email.last).isNotNull();
        assertThat(email.last.getRecipient()).isEqualTo("qa@acme.test");
        assertThat(slack.last).as("slack not used when EMAIL is chosen").isNull();
    }

    @Test
    void nullEventFailsGracefully() {
        assertThat(router.route(null).isDelivered()).isFalse();
    }
}
