package com.aiqaos.eval.contract;

import java.util.List;

/**
 * Scores the quality of a RAG retrieval: how well {@code retrieved} matches the
 * {@code relevant} (ground-truth) set at cut-off {@code k}.
 *
 * <p>MOD-3 defines this seam only; a concrete precision@k / recall implementation over
 * {@code ai-qa-os-memory} retrieval is future work (FI-MOD3-A).
 */
public interface RetrievalQualityMetric {

    /** Stable, human-readable metric name (e.g. {@code "precision@k"}). */
    String getName();

    /**
     * @param retrieved the ids/keys returned by retrieval, in rank order
     * @param relevant  the ground-truth relevant ids/keys
     * @param k         the rank cut-off
     * @return a normalised score in {@code [0,1]}
     */
    double score(List<String> retrieved, List<String> relevant, int k);
}
