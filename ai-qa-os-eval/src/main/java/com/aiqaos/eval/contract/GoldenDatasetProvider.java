package com.aiqaos.eval.contract;

import java.util.List;

/**
 * Supplies the golden {@link EvaluationCase}s for a named suite. MOD-3 ships an in-memory
 * reference provider; <b>PE-1</b> replaces/augments it with durable, versioned datasets
 * (optionally vector-backed via {@code ai-qa-os-memory}).
 */
public interface GoldenDatasetProvider {

    /** Return the cases for {@code suite}, or an empty list if the suite is unknown. */
    List<EvaluationCase> load(String suite);
}
