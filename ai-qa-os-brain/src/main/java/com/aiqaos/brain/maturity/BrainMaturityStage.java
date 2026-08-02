package com.aiqaos.brain.maturity;

/**
 * BRAIN-1 (ADR-082): the QA Brain's staged autonomy ladder — six cumulative stages from human-driven
 * to fully autonomous. Maturity is cumulative: a later stage is only "attained" once every earlier
 * stage's capabilities are present. Stage 5 (AUTONOMOUS) is aspirational (Vision v3.x).
 */
public enum BrainMaturityStage {

    ASSISTED(0, "Assisted", "Rule-based decisions; human-driven."),
    ADVISORY(1, "Advisory", "LLM-assisted recommendations behind a confidence gate."),
    SUPERVISED(2, "Supervised", "Auto-apply within confidence thresholds; humans review exceptions."),
    ADAPTIVE(3, "Adaptive", "Learns and adapts from feedback."),
    ORCHESTRATED(4, "Orchestrated", "Self-directed planning across workflows."),
    AUTONOMOUS(5, "Autonomous", "Fully self-governing under governance guardrails (vision).");

    private final int level;
    private final String displayName;
    private final String summary;

    BrainMaturityStage(int level, String displayName, String summary) {
        this.level = level;
        this.displayName = displayName;
        this.summary = summary;
    }

    public int getLevel() { return level; }
    public String getDisplayName() { return displayName; }
    public String getSummary() { return summary; }
}
