package com.aiqaos.execution.failure;

import com.aiqaos.core.failure.BrokenLocatorSignal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HEAL-3 (FI-HEAL3-A): reads the failing locator out of a Playwright failure, or reports nothing.
 *
 * <p><b>Why this is extraction and not fabrication.</b> ADR-072 deferred FI-HEAL3-A partly because
 * "deriving a broken locator from Playwright error text would be fabrication (ADR-063)". Inspecting
 * real reporter output changes that assessment in one specific case. On a locator failure Playwright
 * emits its own machine-generated call log naming the selector <i>the script supplied</i>:
 *
 * <pre>
 * Error: page.fill: Test timeout of 8000ms exceeded.
 * Call log:
 *   - waiting for locator('#username')
 * </pre>
 *
 * <p>Reading {@code #username} there is not inferring which element the author meant — it is reading
 * which selector Playwright actually waited on and failed. A navigation failure, by contrast, emits
 * no locator at all ({@code - navigating to "http://…"}), so the two are cleanly separable rather
 * than needing to be told apart by guesswork.
 *
 * <p><b>What keeps it honest.</b> The call log is human-readable output, not a documented API, so
 * this refuses rather than approximates:
 * <ul>
 *   <li>no locator token → <b>nothing</b> recorded (navigation failures, assertion mismatches,
 *       infrastructure errors are not locator drift and must not be counted as it);</li>
 *   <li><b>more than one distinct</b> locator in the failure → nothing recorded, because which one
 *       broke would be a guess;</li>
 *   <li>every signal carries {@link BrokenLocatorSignal.Provenance}, so a consumer can weigh it.</li>
 * </ul>
 * The failure mode is therefore an empty drift ranking, never a wrong one — the same discipline as
 * LRN-3's recorder skipping runs whose confidence was never measured.
 */
public class BrokenLocatorExtractor {

    /**
     * Playwright's call-log line. Matches {@code locator('…')} and the {@code getBy*("…")} builders,
     * single or double quoted. Non-greedy so a selector containing a quote-like character does not
     * swallow the rest of the line.
     */
    private static final Pattern LOCATOR_IN_CALL_LOG = Pattern.compile(
            "waiting for (?:locator|getBy\\w+)\\(\\s*['\"](.+?)['\"]");

    /** The failing API call, e.g. {@code page.fill} in "Error: page.fill: Test timeout …". */
    private static final Pattern FAILING_ACTION = Pattern.compile(
            "(?:Error|TimeoutError):\\s*([a-zA-Z]+\\.[a-zA-Z]+):");

    /** ANSI colour codes: the reporter emits them, and they would corrupt a captured selector. */
    private static final Pattern ANSI = Pattern.compile("\\[[0-9;]*m");

    /**
     * @param testCaseId    the test the failure belongs to
     * @param errorMessages every error message Playwright attached to the result — the locator
     *                      detail lives in a later entry than the bare "Test timeout" one, so all of
     *                      them are considered together
     * @return the broken locator, or empty when the failure was not an unambiguous locator failure
     */
    public Optional<BrokenLocatorSignal> extract(String testCaseId, Iterable<String> errorMessages) {
        if (errorMessages == null) {
            return Optional.empty();
        }

        Set<String> selectors = new LinkedHashSet<>();
        String failingAction = null;

        for (String raw : errorMessages) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String message = ANSI.matcher(raw).replaceAll("");

            Matcher locators = LOCATOR_IN_CALL_LOG.matcher(message);
            while (locators.find()) {
                String selector = locators.group(1).trim();
                if (!selector.isEmpty()) {
                    selectors.add(selector);
                }
            }
            if (failingAction == null) {
                Matcher action = FAILING_ACTION.matcher(message);
                if (action.find()) {
                    failingAction = action.group(1);
                }
            }
        }

        // Zero → not a locator failure. More than one → which broke is a guess. Neither is recorded.
        if (selectors.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(new BrokenLocatorSignal(testCaseId, selectors.iterator().next(),
                failingAction, BrokenLocatorSignal.Provenance.PLAYWRIGHT_CALL_LOG));
    }
}
