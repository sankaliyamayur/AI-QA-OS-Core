package com.aiqaos.dashboard.dto;

/**
 * HEAL-3 (FI-HEAL3-B): one row of the most-drifting-locators ranking.
 *
 * @param selector     the failing selector, verbatim as the test supplied it
 * @param failures     how many times this locator has been observed failing
 * @param healsProposed for how many of those a replacement was proposed
 * @param healRate     {@code healsProposed / failures}, 0..1 — surfaced so a reader can tell
 *                     "breaks often but self-heals" from "breaks often and nobody can fix it",
 *                     which are very different maintenance problems
 */
public record LocatorDriftEntry(String selector, long failures, long healsProposed, double healRate) {

    public static LocatorDriftEntry of(String selector, long failures, long healsProposed) {
        double rate = failures > 0 ? (double) healsProposed / failures : 0.0;
        return new LocatorDriftEntry(selector, failures, healsProposed, rate);
    }
}
