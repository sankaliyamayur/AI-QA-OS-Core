package com.aiqaos.dashboard.service;

import com.aiqaos.eval.benchmark.PromptRegressionReport;
import com.aiqaos.eval.benchmark.PromptRegressionSignal;
import com.aiqaos.notification.event.NotificationEvent;
import com.aiqaos.notification.event.NotificationEventRouter;
import com.aiqaos.notification.event.NotificationEventType;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * FI-PE3-D (PE-3): pushes prompt-quality regressions through ENT-2's governed notification path.
 *
 * <p>FI-PE3-B (ADR-069) can already detect that a prompt version's recent evaluation scores have
 * declined, but only if somebody opens the dashboard. A quality decline is exactly the failure mode
 * nobody goes looking for — nothing errors, output just gets worse — so it has to be pushed. This
 * turns each newly-detected regression into a {@link NotificationEventType#PROMPT_REGRESSION} event
 * routed via {@link NotificationEventRouter}, the one governed egress point (ENT-2 / MOD-2).
 *
 * <p><b>Alerts on transitions, not on state.</b> A regression persists until the version recovers or
 * is retired, so re-alerting every sweep would be pure noise and would train recipients to ignore it.
 * Already-alerted versions are remembered and skipped; a version that stops regressing is forgotten,
 * so a genuine re-regression alerts again. That memory is in-process — a restart re-alerts once for
 * still-regressed versions, which is the safe direction (a missed alert is worse than a repeated one)
 * and is why it is not persisted.
 *
 * <p><b>{@code @Scheduled} is safe here</b>, unlike in the gateway: the dashboard's
 * {@code DashboardApplication} declares {@code @EnableScheduling} (see ADR-084, where the gateway's
 * lack of it forced a self-owned timer). Opt-in via {@code aiqaos.eval.regression.alerts.enabled}, so
 * no deployment starts sending alerts by upgrading.
 */
@Service
@ConditionalOnProperty(name = "aiqaos.eval.regression.alerts.enabled", havingValue = "true")
public class PromptRegressionAlerter {

    private static final Logger log = LoggerFactory.getLogger(PromptRegressionAlerter.class);

    private final PromptQualityService promptQualityService;
    private final NotificationEventRouter notificationEventRouter;
    private final String recipient;

    /** Versions already alerted on, so a persistent regression is reported once, not every sweep. */
    private final Set<String> alreadyAlerted = ConcurrentHashMap.newKeySet();

    public PromptRegressionAlerter(PromptQualityService promptQualityService,
                                   NotificationEventRouter notificationEventRouter,
                                   @Value("${aiqaos.eval.regression.alerts.recipient:qa-leads}") String recipient) {
        this.promptQualityService = promptQualityService;
        this.notificationEventRouter = notificationEventRouter;
        this.recipient = recipient;
    }

    /**
     * Scheduled sweep. Swallows everything: an exception thrown back at Spring's scheduler cancels the
     * repeating trigger, which would silently disable alerting for the life of the process.
     */
    @Scheduled(cron = "${aiqaos.eval.regression.alerts.cron:0 0 8 * * *}")
    public void scheduledSweep() {
        try {
            sweep();
        } catch (Exception e) {
            log.error("[prompt-regression-alerts] sweep failed", e);
        }
    }

    /**
     * Detect regressions and alert on the ones not yet reported. Returns how many alerts were sent.
     * Exposed for unit proof and on-demand ops use.
     */
    public int sweep() {
        PromptRegressionReport report = promptQualityService.getRegressions();
        if (report == null) {
            return 0;
        }

        Set<String> currentlyRegressed = new HashSet<>();
        int alerted = 0;
        for (PromptRegressionSignal signal : report.regressions()) {
            if (signal == null || signal.versionId() == null || signal.versionId().isBlank()) {
                continue;
            }
            currentlyRegressed.add(signal.versionId());
            if (alreadyAlerted.add(signal.versionId())) {
                alerted += alert(signal, report.tolerance()) ? 1 : 0;
            }
        }

        // Forget recovered versions so a genuine re-regression alerts again rather than being
        // permanently suppressed by the first occurrence.
        alreadyAlerted.retainAll(currentlyRegressed);

        if (alerted > 0) {
            log.info("[prompt-regression-alerts] alerted on {} newly-regressed prompt version(s)", alerted);
        }
        return alerted;
    }

    private boolean alert(PromptRegressionSignal signal, double tolerance) {
        try {
            notificationEventRouter.route(NotificationEvent.of(
                    NotificationEventType.PROMPT_REGRESSION, signal.versionId(), summary(signal, tolerance), recipient));
            return true;
        } catch (Exception e) {
            // Delivery must never break the sweep — and the version must be re-alertable next time.
            alreadyAlerted.remove(signal.versionId());
            log.warn("[prompt-regression-alerts] failed to alert on {}: {}", signal.versionId(), e.getMessage());
            return false;
        }
    }

    private String summary(PromptRegressionSignal s, double tolerance) {
        return String.format(
                "Prompt version %s regressed: recent mean %.3f vs earlier %.3f (delta %.3f, tolerance %.3f) "
                        + "over %d evaluation result(s).",
                s.versionId(), s.currentScore(), s.baselineScore(), s.delta(), tolerance, s.sampleCount());
    }
}
