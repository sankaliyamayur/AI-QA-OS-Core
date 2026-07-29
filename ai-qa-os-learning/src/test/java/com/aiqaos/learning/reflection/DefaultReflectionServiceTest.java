package com.aiqaos.learning.reflection;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.model.FailurePattern;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * LRN-1: unit tests for deterministic reflection — pattern → typed improvement proposal, priority,
 * and carried fields. No Spring, no Mockito.
 */
class DefaultReflectionServiceTest {

    private final DefaultReflectionService service = new DefaultReflectionService();

    private FailurePattern pattern(String errorType, String rootCause, String component,
                                   int occurrences, double confidence) {
        FailurePattern p = new FailurePattern();
        p.setPatternId("PAT-1");
        p.setErrorType(errorType);
        p.setRootCause(rootCause);
        p.setImpactedComponent(component);
        p.setOccurrenceCount(occurrences);
        p.setConfidence(confidence);
        return p;
    }

    @Test
    void locatorFailureMapsToAutomation() {
        ReflectionResult r = service.reflect(List.of(
                pattern("LOCATOR_NOT_FOUND", "stale element", "loginButton", 1, 0.4)));
        assertThat(r.getProposals()).singleElement()
                .extracting(ImprovementProposal::getType).isEqualTo(ProposalType.AUTOMATION);
    }

    @Test
    void modelFailureMapsToPrompt() {
        ReflectionResult r = service.reflect(List.of(
                pattern("MODEL_HALLUCINATION", "prompt drift", "scenarioGen", 1, 0.5)));
        assertThat(r.getProposals().get(0).getType()).isEqualTo(ProposalType.PROMPT);
    }

    @Test
    void coverageGapMapsToScenario() {
        ReflectionResult r = service.reflect(List.of(
                pattern("COVERAGE_GAP", "missing assertion", "checkoutFlow", 1, 0.5)));
        assertThat(r.getProposals().get(0).getType()).isEqualTo(ProposalType.SCENARIO);
    }

    @Test
    void unknownErrorDefaultsToAutomation() {
        ReflectionResult r = service.reflect(List.of(
                pattern("GENERIC_ERROR", "unknown", "cart", 1, 0.3)));
        assertThat(r.getProposals().get(0).getType()).isEqualTo(ProposalType.AUTOMATION);
    }

    @Test
    void recurringPatternIsHighPriorityAndFlagged() {
        ReflectionResult r = service.reflect(List.of(
                pattern("LOCATOR_NOT_FOUND", "stale", "btn", 3, 0.4))); // occ >= threshold(3)
        ImprovementProposal p = r.getProposals().get(0);
        assertThat(p.isRecurring()).isTrue();
        assertThat(p.getPriority()).isEqualTo(ImprovementProposal.Priority.HIGH);
    }

    @Test
    void highConfidenceRaisesPriorityEvenIfNotRecurring() {
        ImprovementProposal p = service.reflect(List.of(
                pattern("GENERIC_ERROR", "x", "y", 1, 0.9))).getProposals().get(0);
        assertThat(p.isRecurring()).isFalse();
        assertThat(p.getPriority()).isEqualTo(ImprovementProposal.Priority.HIGH);
    }

    @Test
    void carriesConfidenceTargetAndSourcePattern() {
        ImprovementProposal p = service.reflect(List.of(
                pattern("LOCATOR", "stale", "searchBox", 1, 0.42))).getProposals().get(0);
        assertThat(p.getConfidence()).isEqualTo(0.42);
        assertThat(p.getTargetComponent()).isEqualTo("searchBox");
        assertThat(p.getSourcePatternId()).isEqualTo("PAT-1");
        assertThat(p.getProposalId()).startsWith("IMP-");
    }

    @Test
    void emptyAndNullInputsYieldNoProposals() {
        assertThat(service.reflect(List.of()).getCount()).isZero();
        assertThat(service.reflect(null).getCount()).isZero();
    }

    @Test
    void countsByTypeAggregateAcrossPatterns() {
        ReflectionResult r = service.reflect(List.of(
                pattern("LOCATOR", "a", "c1", 1, 0.3),
                pattern("MODEL_LLM", "b", "c2", 1, 0.3),
                pattern("SCENARIO_MISSING", "c", "c3", 1, 0.3)));
        assertThat(r.getCount()).isEqualTo(3);
        assertThat(r.countsByType())
                .containsEntry(ProposalType.AUTOMATION, 1)
                .containsEntry(ProposalType.PROMPT, 1)
                .containsEntry(ProposalType.SCENARIO, 1);
    }
}
