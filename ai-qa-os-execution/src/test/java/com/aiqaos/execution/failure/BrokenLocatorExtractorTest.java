package com.aiqaos.execution.failure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.core.failure.BrokenLocatorSignal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * HEAL-3 (FI-HEAL3-A). The fixtures below are <b>real</b> Playwright 1.x reporter output captured
 * from this repo's own specs on 2026-08-10 — a locator failure (stub page missing {@code #username})
 * and a navigation failure (nothing listening) — not invented strings. That matters: the whole
 * premise is that the call log reports a selector faithfully, so the test has to be written against
 * what the tool actually emits.
 */
class BrokenLocatorExtractorTest {

    private final BrokenLocatorExtractor extractor = new BrokenLocatorExtractor();

    /** Playwright attaches the bare timeout first and the actionable detail second. */
    private static final List<String> REAL_LOCATOR_FAILURE = List.of(
            "Test timeout of 8000ms exceeded.",
            """
            Error: page.fill: Test timeout of 8000ms exceeded.
            Call log:
              - waiting for locator('#username')


            > 1 | import { test, expect } from '@playwright/test'; test('valid login navigation', async ({ page }) => { await page.goto('http://localhost:3000'); await page.fill('#username', 'admin'); });
                |                                                    ^
                at TC-001.spec.ts:1:156
            """);

    /** No target listening — a real failure, but not locator drift. */
    private static final List<String> REAL_NAVIGATION_FAILURE = List.of(
            """
            Error: page.goto: net::ERR_CONNECTION_REFUSED at http://localhost:3000/
            Call log:
              - navigating to "http://localhost:3000/", waiting until "load"
            """);

    @Test
    void readsTheSelectorPlaywrightSaysItWasWaitingOn() {
        Optional<BrokenLocatorSignal> signal = extractor.extract("TC-001", REAL_LOCATOR_FAILURE);

        assertTrue(signal.isPresent());
        assertEquals("#username", signal.get().getSelector());
        assertEquals("TC-001", signal.get().getTestCaseId());
        assertEquals("page.fill", signal.get().getFailingAction());
        assertEquals(BrokenLocatorSignal.Provenance.PLAYWRIGHT_CALL_LOG, signal.get().getProvenance());
    }

    @Test
    void aNavigationFailureIsNotLocatorDrift() {
        // The single most important negative: counting these would make every run with an unreachable
        // target look like mass locator drift.
        assertTrue(extractor.extract("TC-001", REAL_NAVIGATION_FAILURE).isEmpty());
    }

    @Test
    void severalDistinctLocatorsMeanWhichOneBrokeIsAGuess() {
        List<String> ambiguous = List.of("""
                Error: page.click: Test timeout exceeded.
                Call log:
                  - waiting for locator('#username')
                  - waiting for locator('button[type=submit]')
                """);

        assertTrue(extractor.extract("TC-001", ambiguous).isEmpty(),
                "recording either one would be fabrication; an empty ranking is the honest outcome");
    }

    @Test
    void theSameLocatorRepeatedIsStillOneLocator() {
        List<String> retried = List.of("""
                Error: page.click: Test timeout exceeded.
                Call log:
                  - waiting for locator('#submit')
                  - waiting for locator('#submit')
                """);

        assertEquals("#submit", extractor.extract("TC-001", retried).orElseThrow().getSelector());
    }

    @Test
    void handlesTheGetByBuilders() {
        List<String> byRole = List.of("""
                Error: page.click: Test timeout exceeded.
                Call log:
                  - waiting for getByRole('button')
                """);

        assertEquals("button", extractor.extract("TC-001", byRole).orElseThrow().getSelector());
    }

    @Test
    void stripsAnsiColourCodesTheReporterEmits() {
        // The reporter colourises; left in, they would corrupt the selector used as the drift key.
        List<String> coloured = List.of(
                "Error: page.fill: timeout\nCall log:\n  - waiting for locator([2m'[22m#username[2m'[22m)");

        assertEquals("#username", extractor.extract("TC-001", coloured).orElseThrow().getSelector());
    }

    @Test
    void assertionFailuresAndBlankInputAreIgnored() {
        assertTrue(extractor.extract("TC-001", List.of(
                "Error: expect(received).toBeVisible()\n\nReceived: hidden")).isEmpty());
        assertTrue(extractor.extract("TC-001", List.of("", "   ")).isEmpty());
        assertTrue(extractor.extract("TC-001", null).isEmpty());
    }
}
