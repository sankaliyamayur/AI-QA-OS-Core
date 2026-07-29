package com.aiqaos.core.contract;

/**
 * LRN-4: the outcome of a {@link SafeAdoptionGate} evaluation. A rejected candidate is
 * <em>logged for human review</em>, never silently dropped — so learning stays monotonic.
 */
public enum AdoptionVerdict {
    ADMITTED,
    REJECTED_FOR_REVIEW
}
