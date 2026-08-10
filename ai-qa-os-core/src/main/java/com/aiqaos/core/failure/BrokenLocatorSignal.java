package com.aiqaos.core.failure;

/**
 * HEAL-3 (FI-HEAL3-A): a locator that a test run demonstrably failed on.
 *
 * <p>This is the structured broken-locator source whose absence kept FI-HEAL3-A blocked (ADR-072).
 * It carries only what Playwright itself reported — the selector string it was waiting on, the test
 * that failed, and where the value came from — so nothing downstream has to guess.
 *
 * <p>{@code selector} is <b>verbatim</b> as the script supplied it. It is deliberately not
 * normalised or prettified: it is the identity used to correlate repeated failures of the same
 * locator, and rewriting it would split one drifting locator into several.
 */
public final class BrokenLocatorSignal {

    /** How the selector was obtained. Recorded so a consumer can weigh it, not just trust it. */
    public enum Provenance {
        /** Read from Playwright's own "Call log: - waiting for locator('…')" line. */
        PLAYWRIGHT_CALL_LOG
    }

    private final String testCaseId;
    private final String selector;
    private final String failingAction;
    private final Provenance provenance;

    public BrokenLocatorSignal(String testCaseId, String selector, String failingAction,
                               Provenance provenance) {
        this.testCaseId = testCaseId;
        this.selector = selector;
        this.failingAction = failingAction;
        this.provenance = provenance;
    }

    public String getTestCaseId() { return testCaseId; }

    /** The selector exactly as the test supplied it, e.g. {@code #username}. */
    public String getSelector() { return selector; }

    /** The Playwright call that failed, e.g. {@code page.fill} — null when it could not be read. */
    public String getFailingAction() { return failingAction; }

    public Provenance getProvenance() { return provenance; }

    @Override
    public String toString() {
        return "BrokenLocatorSignal{test=" + testCaseId + ", selector='" + selector
                + "', action=" + failingAction + ", via=" + provenance + "}";
    }
}
