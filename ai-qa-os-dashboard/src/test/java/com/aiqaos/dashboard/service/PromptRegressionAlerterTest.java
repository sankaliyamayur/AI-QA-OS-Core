package com.aiqaos.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.eval.benchmark.PromptRegressionReport;
import com.aiqaos.eval.benchmark.PromptRegressionSignal;
import com.aiqaos.notification.NotificationResult;
import com.aiqaos.notification.event.NotificationEvent;
import com.aiqaos.notification.event.NotificationEventRouter;
import com.aiqaos.notification.event.NotificationEventType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

/**
 * FI-PE3-D: prompt regressions must reach a human. The behaviour that matters most here is
 * <em>alerting on transitions</em> — a regression persists until it is fixed, so re-alerting every
 * sweep would train recipients to ignore the alert entirely.
 */
class PromptRegressionAlerterTest {

    @Test
    void alertsOnANewlyDetectedRegression() {
        List<NotificationEvent> routed = new ArrayList<>();
        PromptRegressionAlerter alerter = alerter(report(signal("v2", 0.90, 0.60)), routed);

        assertEquals(1, alerter.sweep());
        assertEquals(1, routed.size());
        NotificationEvent event = routed.get(0);
        assertEquals(NotificationEventType.PROMPT_REGRESSION, event.getType());
        assertEquals("v2", event.getSubjectRef());
        assertEquals("qa-leads", event.getRecipient());
        assertTrue(event.getSummary().contains("v2"));
        assertTrue(event.getSummary().contains("0.600"), "carries the observed recent mean");
        assertTrue(event.getSummary().contains("0.900"), "carries the observed baseline");
    }

    @Test
    void doesNotRealertWhileTheSameRegressionPersists() {
        List<NotificationEvent> routed = new ArrayList<>();
        PromptRegressionAlerter alerter = alerter(report(signal("v2", 0.90, 0.60)), routed);

        assertEquals(1, alerter.sweep());
        assertEquals(0, alerter.sweep(), "a persistent regression must not re-alert every sweep");
        assertEquals(0, alerter.sweep());
        assertEquals(1, routed.size());
    }

    @Test
    void realertsWhenAVersionRecoversThenRegressesAgain() {
        List<NotificationEvent> routed = new ArrayList<>();
        AtomicReference<PromptRegressionReport> current =
                new AtomicReference<>(report(signal("v2", 0.90, 0.60)));
        PromptRegressionAlerter alerter = alerter(current, routed);

        assertEquals(1, alerter.sweep());
        current.set(report()); // recovered — no longer flagged
        assertEquals(0, alerter.sweep());
        current.set(report(signal("v2", 0.90, 0.55))); // regressed again
        assertEquals(1, alerter.sweep(), "a genuine re-regression must alert again");
        assertEquals(2, routed.size());
    }

    @Test
    void alertsSeparatelyForEachRegressedVersion() {
        List<NotificationEvent> routed = new ArrayList<>();
        PromptRegressionAlerter alerter =
                alerter(report(signal("v2", 0.9, 0.6), signal("v3", 0.8, 0.5)), routed);

        assertEquals(2, alerter.sweep());
        assertTrue(routed.stream().anyMatch(e -> "v2".equals(e.getSubjectRef())));
        assertTrue(routed.stream().anyMatch(e -> "v3".equals(e.getSubjectRef())));
    }

    @Test
    void anEmptyReportAlertsNothing() {
        List<NotificationEvent> routed = new ArrayList<>();

        assertEquals(0, alerter(report(), routed).sweep());
        assertTrue(routed.isEmpty());
    }

    @Test
    void skipsSignalsWithNoVersionId() {
        List<NotificationEvent> routed = new ArrayList<>();
        PromptRegressionAlerter alerter =
                alerter(report(signal(null, 0.9, 0.6), signal("  ", 0.9, 0.6)), routed);

        assertEquals(0, alerter.sweep());
        assertTrue(routed.isEmpty());
    }

    @Test
    void aDeliveryFailureLeavesTheVersionRealertable() {
        AtomicReference<PromptRegressionReport> current =
                new AtomicReference<>(report(signal("v2", 0.9, 0.6)));
        boolean[] fail = {true};
        PromptRegressionAlerter alerter = new PromptRegressionAlerter(
                qualityServiceReturning(current),
                new NotificationEventRouter(null, null) {
                    @Override
                    public NotificationResult route(NotificationEvent event) {
                        if (fail[0]) {
                            throw new IllegalStateException("slack unreachable");
                        }
                        return NotificationResult.delivered("ok");
                    }
                },
                "qa-leads");

        assertEquals(0, alerter.sweep(), "a failed delivery is not a successful alert");
        fail[0] = false;
        assertEquals(1, alerter.sweep(), "the regression must be retried, not silently suppressed");
    }

    @Test
    void aNullReportIsHandled() {
        List<NotificationEvent> routed = new ArrayList<>();
        assertEquals(0, alerter(new AtomicReference<>(null), routed).sweep());
    }

    @Test
    void scheduledSweepNeverThrows() {
        PromptRegressionAlerter alerter = new PromptRegressionAlerter(
                new PromptQualityService(null, null, null, 0.05, 4) {
                    @Override
                    public PromptRegressionReport getRegressions() {
                        throw new IllegalStateException("database down");
                    }
                },
                capturingRouter(new ArrayList<>()),
                "qa-leads");

        // an escaping exception would cancel Spring's repeating trigger for the life of the process
        assertDoesNotThrow(alerter::scheduledSweep);
    }

    // --- fakes -----------------------------------------------------------------------------------

    private static PromptRegressionSignal signal(String versionId, double baseline, double current) {
        return new PromptRegressionSignal(versionId, baseline, current, current - baseline, 6);
    }

    private static PromptRegressionReport report(PromptRegressionSignal... signals) {
        return new PromptRegressionReport(0.05, signals.length, List.of(signals));
    }

    private static PromptRegressionAlerter alerter(PromptRegressionReport report,
                                                   List<NotificationEvent> routed) {
        return alerter(new AtomicReference<>(report), routed);
    }

    private static PromptRegressionAlerter alerter(AtomicReference<PromptRegressionReport> report,
                                                   List<NotificationEvent> routed) {
        return new PromptRegressionAlerter(qualityServiceReturning(report), capturingRouter(routed), "qa-leads");
    }

    private static PromptQualityService qualityServiceReturning(AtomicReference<PromptRegressionReport> report) {
        return new PromptQualityService(null, null, null, 0.05, 4) {
            @Override
            public PromptRegressionReport getRegressions() {
                return report.get();
            }
        };
    }

    private static NotificationEventRouter capturingRouter(List<NotificationEvent> sink) {
        return new NotificationEventRouter(null, null) {
            @Override
            public NotificationResult route(NotificationEvent event) {
                sink.add(event);
                return NotificationResult.delivered("captured");
            }
        };
    }
}
