package com.aiqaos.healing.locator;

import com.aiqaos.core.contract.ConfidenceVerdict;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HEAL-1: the result of locator healing — the ranked candidates, the {@code chosen} best, whether it
 * was {@code applied} (cleared the confidence gate), the underlying {@link ConfidenceVerdict}
 * (nullable when no gate was present), and a human-readable reason. A non-applied outcome still
 * carries its chosen candidate so it can be surfaced for review (HEAL-2).
 */
public final class LocatorHealingOutcome {

    private final List<LocatorCandidate> candidates;
    private final LocatorCandidate chosen;         // nullable when no candidate found
    private final boolean applied;
    private final ConfidenceVerdict confidenceVerdict; // nullable
    private final String reason;

    public LocatorHealingOutcome(List<LocatorCandidate> candidates, LocatorCandidate chosen,
                                 boolean applied, ConfidenceVerdict confidenceVerdict, String reason) {
        this.candidates = candidates != null ? new ArrayList<>(candidates) : new ArrayList<>();
        this.chosen = chosen;
        this.applied = applied;
        this.confidenceVerdict = confidenceVerdict;
        this.reason = reason;
    }

    public static LocatorHealingOutcome none(String reason) {
        return new LocatorHealingOutcome(Collections.emptyList(), null, false, null, reason);
    }

    public List<LocatorCandidate> getCandidates() { return candidates; }
    public LocatorCandidate getChosen() { return chosen; }
    public boolean isApplied() { return applied; }
    public ConfidenceVerdict getConfidenceVerdict() { return confidenceVerdict; }
    public String getReason() { return reason; }

    @Override
    public String toString() {
        return "LocatorHealingOutcome{chosen=" + chosen + ", applied=" + applied + ", " + reason + "}";
    }
}
