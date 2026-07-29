package com.aiqaos.core.contract;

/**
 * LRN-4: the kind of learned improvement being gated for adoption — the core-level vocabulary that
 * LRN-1's {@code ProposalType} maps to (kept in {@code core} so the Brain gate needs no dependency
 * on {@code ai-qa-os-learning}).
 */
public enum AdoptionKind {
    PROMPT,
    SCENARIO,
    AUTOMATION
}
