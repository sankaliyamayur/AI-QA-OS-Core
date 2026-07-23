package com.aiqaos.eval.harness;

/**
 * Loads and persists a suite's regression {@link Baseline}. MOD-3's DB persistence keeps a full
 * history; this seam is the harness's stable, reviewable reference point (Option A = a file).
 */
public interface BaselineStore {

    /** Load the baseline for {@code suite}, or an empty baseline if none exists yet. */
    Baseline load(String suite);

    /** Persist {@code baseline} as the new reference (the "update baseline" step). */
    void save(Baseline baseline);
}
