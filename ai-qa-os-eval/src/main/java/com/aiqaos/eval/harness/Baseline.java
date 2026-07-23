package com.aiqaos.eval.harness;

import java.util.HashMap;
import java.util.Map;

/**
 * A suite's regression reference point: the aggregate score each case scored when the baseline
 * was last accepted. Serialised to/from {@code golden/<suite>.baseline.json} (Option A, ADR-013).
 */
public class Baseline {

    private String suite;
    private Map<String, Double> caseScores = new HashMap<>();

    public Baseline() {
    }

    public Baseline(String suite, Map<String, Double> caseScores) {
        this.suite = suite;
        this.caseScores = caseScores == null ? new HashMap<>() : new HashMap<>(caseScores);
    }

    /** The baseline score for a case, or {@code null} if the case has no baseline yet. */
    public Double scoreFor(String caseId) {
        return caseScores.get(caseId);
    }

    public String getSuite() {
        return suite;
    }

    public void setSuite(String suite) {
        this.suite = suite;
    }

    public Map<String, Double> getCaseScores() {
        return caseScores;
    }

    public void setCaseScores(Map<String, Double> caseScores) {
        this.caseScores = caseScores == null ? new HashMap<>() : caseScores;
    }
}
