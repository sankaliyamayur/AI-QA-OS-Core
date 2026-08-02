package com.aiqaos.brain.maturity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * BRAIN-1 (ADR-082): the curated maturity ladder — each {@link BrainMaturityStage} mapped to the
 * capability keys that characterise it. Capability keys are the vocabulary a real capability inventory
 * reports (the shipped brain components: rule/llm/hybrid decision strategies, confidence gate, healing
 * approval, learning engine, planners). Stage AUTONOMOUS requires a capability that is aspirational, so
 * it is never auto-attained unless a runtime explicitly declares it.
 */
@Component
public class BrainMaturityModel {

    public static final String CAP_RULE_BASED = "rule-based-decisions";
    public static final String CAP_LLM = "llm-decisions";
    public static final String CAP_CONFIDENCE_GATE = "confidence-gate";
    public static final String CAP_HYBRID = "hybrid-decisions";
    public static final String CAP_HEALING_APPROVAL = "healing-approval";
    public static final String CAP_LEARNING = "learning-engine";
    public static final String CAP_FEEDBACK = "feedback-processor";
    public static final String CAP_QA_PLANNER = "qa-planner";
    public static final String CAP_TEST_STRATEGY_PLANNER = "test-strategy-planner";
    public static final String CAP_AUTONOMOUS_GOVERNANCE = "autonomous-governance";

    private final Map<BrainMaturityStage, Set<String>> required = new LinkedHashMap<>();

    public BrainMaturityModel() {
        required.put(BrainMaturityStage.ASSISTED, Set.of(CAP_RULE_BASED));
        required.put(BrainMaturityStage.ADVISORY, Set.of(CAP_LLM, CAP_CONFIDENCE_GATE));
        required.put(BrainMaturityStage.SUPERVISED, Set.of(CAP_HYBRID, CAP_HEALING_APPROVAL));
        required.put(BrainMaturityStage.ADAPTIVE, Set.of(CAP_LEARNING, CAP_FEEDBACK));
        required.put(BrainMaturityStage.ORCHESTRATED, Set.of(CAP_QA_PLANNER, CAP_TEST_STRATEGY_PLANNER));
        required.put(BrainMaturityStage.AUTONOMOUS, Set.of(CAP_AUTONOMOUS_GOVERNANCE));
    }

    /** The capability keys a stage requires. */
    public Set<String> requiredCapabilities(BrainMaturityStage stage) {
        return required.getOrDefault(stage, Set.of());
    }

    /** Stages ordered by level (0 → 5). */
    public List<BrainMaturityStage> stages() {
        return Arrays.stream(BrainMaturityStage.values())
                .sorted(Comparator.comparingInt(BrainMaturityStage::getLevel))
                .toList();
    }
}
