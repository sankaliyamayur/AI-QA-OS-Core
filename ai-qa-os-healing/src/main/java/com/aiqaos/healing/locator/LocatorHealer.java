package com.aiqaos.healing.locator;

import java.util.List;

/**
 * HEAL-1 seam: propose alternative locators for a broken one. The reference
 * {@link HeuristicLocatorHealer} is deterministic (attribute/strategy heuristics); an LLM-backed
 * healer can implement this same seam later (FI-HEAL1-B) without touching the governance
 * ({@link LocatorHealingService}) or the engine.
 */
public interface LocatorHealer {

    /** Ranked best-first list of candidate replacement locators (empty if none can be proposed). */
    List<LocatorCandidate> propose(LocatorHealingRequest request);
}
