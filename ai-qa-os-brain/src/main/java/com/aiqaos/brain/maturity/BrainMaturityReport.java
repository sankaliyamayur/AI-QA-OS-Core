package com.aiqaos.brain.maturity;

import java.util.List;
import java.util.Set;

/**
 * BRAIN-1 (ADR-082): the brain maturity self-assessment — the attained stage plus a per-stage
 * breakdown. Self-assessment over declared capabilities, not a claim of autonomy.
 *
 * @param attestation   the self-assessment disclaimer
 * @param attainedStage the highest cumulative stage fully satisfied ({@code null} if none — below ASSISTED)
 * @param stages        per-stage satisfaction + the capabilities each stage is missing
 */
public record BrainMaturityReport(
        String attestation,
        BrainMaturityStage attainedStage,
        List<StageAssessment> stages) {

    /** Whether a stage's required capabilities are all present, and which are missing if not. */
    public record StageAssessment(BrainMaturityStage stage, boolean satisfied, Set<String> missing) {
    }
}
