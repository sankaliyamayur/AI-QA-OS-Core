package com.aiqaos.learning.reflection;

import com.aiqaos.core.model.FailurePattern;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * LRN-1: deterministic reflection — maps each root-caused {@link FailurePattern} to a typed
 * {@link ImprovementProposal} by its error signature. Locator/element failures suggest an
 * <em>automation</em> fix, model/prompt failures a <em>prompt</em> improvement, and coverage/assertion
 * gaps a <em>scenario</em> improvement; anything else defaults to a low-priority automation
 * stabilisation. Recurring or high-confidence patterns are raised to {@code HIGH} priority.
 *
 * <p>Pure and deterministic (aside from proposal ids): fully unit-testable. Proposals are
 * <em>recorded</em> by the loop, never auto-applied (adoption is gated on LRN-4).
 */
@Component
public class DefaultReflectionService implements ReflectionService {

    /** A pattern seen at least this many times is treated as recurring → HIGH priority. */
    static final int DEFAULT_RECURRING_THRESHOLD = 3;
    /** Confidence at/above this also raises priority. */
    static final double HIGH_CONFIDENCE = 0.8;

    private final int recurringThreshold;

    public DefaultReflectionService() {
        this(DEFAULT_RECURRING_THRESHOLD);
    }

    DefaultReflectionService(int recurringThreshold) {
        this.recurringThreshold = recurringThreshold;
    }

    @Override
    public ReflectionResult reflect(List<FailurePattern> patterns) {
        List<ImprovementProposal> proposals = new ArrayList<>();
        if (patterns == null) {
            return new ReflectionResult(proposals);
        }
        for (FailurePattern pattern : patterns) {
            if (pattern != null) {
                proposals.add(toProposal(pattern));
            }
        }
        return new ReflectionResult(proposals);
    }

    private ImprovementProposal toProposal(FailurePattern pattern) {
        String signature = (safe(pattern.getErrorType()) + " " + safe(pattern.getRootCause())
                + " " + safe(pattern.getImpactedComponent())).toUpperCase();

        ProposalType type = classify(signature);
        boolean recurring = pattern.getOccurrenceCount() >= recurringThreshold;
        ImprovementProposal.Priority priority =
                (recurring || pattern.getConfidence() >= HIGH_CONFIDENCE)
                        ? ImprovementProposal.Priority.HIGH
                        : ImprovementProposal.Priority.NORMAL;

        return new ImprovementProposal(
                "IMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                type,
                pattern.getImpactedComponent(),
                rationale(type, pattern),
                pattern.getPatternId(),
                pattern.getConfidence(),
                recurring,
                priority);
    }

    private ProposalType classify(String signature) {
        if (containsAny(signature, "LOCATOR", "SELECTOR", "ELEMENT", "XPATH", "STALE",
                "NOSUCHELEMENT", "TIMEOUT", "CLICK")) {
            return ProposalType.AUTOMATION;
        }
        if (containsAny(signature, "PROMPT", "HALLUCINAT", "GENERATION", "LLM", "MODEL", "AI_")) {
            return ProposalType.PROMPT;
        }
        if (containsAny(signature, "COVERAGE", "SCENARIO", "MISSING", "ASSERTION", "UNCOVERED",
                "EXPECTATION")) {
            return ProposalType.SCENARIO;
        }
        return ProposalType.AUTOMATION; // generic default: stabilise the automation
    }

    private String rationale(ProposalType type, FailurePattern pattern) {
        String cause = pattern.getRootCause() != null ? pattern.getRootCause() : pattern.getErrorType();
        switch (type) {
            case PROMPT:
                return "Recurring model/prompt failure (" + cause + ") — refine the prompt for "
                        + pattern.getImpactedComponent();
            case SCENARIO:
                return "Coverage/assertion gap (" + cause + ") — extend scenarios for "
                        + pattern.getImpactedComponent();
            case AUTOMATION:
            default:
                return "Automation/locator failure (" + cause + ") — repair the script for "
                        + pattern.getImpactedComponent();
        }
    }

    private static boolean containsAny(String haystack, String... needles) {
        for (String n : needles) {
            if (haystack.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
