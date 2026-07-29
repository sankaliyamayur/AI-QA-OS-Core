package com.aiqaos.learning.adoption;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.contract.AdoptionCandidate;
import com.aiqaos.core.contract.AdoptionDecision;
import com.aiqaos.core.contract.AdoptionKind;
import com.aiqaos.core.contract.AdoptionVerdict;
import com.aiqaos.core.contract.SafeAdoptionGate;
import com.aiqaos.learning.reflection.ImprovementProposal;
import com.aiqaos.learning.reflection.ProposalType;
import com.aiqaos.learning.reflection.ReflectionResult;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * FI-LRN4-A: unit tests for wiring LRN-1 proposals through the LRN-4 safe-adoption gate. Uses a
 * capturing gate stub (admits iff evalScore ≥ 0.75 and confidence ≥ 0.90). No Mockito.
 */
class ProposalAdoptionCoordinatorTest {

    /** Captures the candidate it was handed, and admits on high eval + high confidence. */
    private static final class CapturingGate implements SafeAdoptionGate {
        AdoptionCandidate last;
        @Override
        public AdoptionDecision evaluate(AdoptionCandidate candidate) {
            this.last = candidate;
            boolean admit = candidate.getEvalScore() >= 0.75 && candidate.getConfidence() >= 0.90;
            return admit
                    ? AdoptionDecision.admitted(candidate.getCandidateId(), null, "ok")
                    : AdoptionDecision.rejected(candidate.getCandidateId(), null, "below threshold");
        }
    }

    private ImprovementProposal proposal(ProposalType type, double confidence) {
        return new ImprovementProposal("IMP-1", type, "loginPrompt", "refine prompt",
                "PAT-1", confidence, false, ImprovementProposal.Priority.NORMAL);
    }

    @Test
    void admitsHighQualityHighConfidenceProposal() {
        CapturingGate gate = new CapturingGate();
        ProposalAdoptionCoordinator coord = new ProposalAdoptionCoordinator(gate);

        AdoptionDecision d = coord.evaluate(proposal(ProposalType.PROMPT, 0.95), 0.90, "c1");

        assertThat(d.getVerdict()).isEqualTo(AdoptionVerdict.ADMITTED);
        assertThat(gate.last.getEvalScore()).isEqualTo(0.90);
        assertThat(gate.last.getConfidence()).isEqualTo(0.95);
    }

    @Test
    void rejectsLowEvalScore() {
        ProposalAdoptionCoordinator coord = new ProposalAdoptionCoordinator(new CapturingGate());
        AdoptionDecision d = coord.evaluate(proposal(ProposalType.PROMPT, 0.95), 0.50, "c1");
        assertThat(d.getVerdict()).isEqualTo(AdoptionVerdict.REJECTED_FOR_REVIEW);
    }

    @Test
    void mapsProposalTypeToAdoptionKind() {
        CapturingGate gate = new CapturingGate();
        ProposalAdoptionCoordinator coord = new ProposalAdoptionCoordinator(gate);
        coord.evaluate(proposal(ProposalType.AUTOMATION, 0.95), 0.90, "c1");
        assertThat(gate.last.getKind()).isEqualTo(AdoptionKind.AUTOMATION);
    }

    @Test
    void failsSafeWhenNoGatePresent() {
        ProposalAdoptionCoordinator coord = new ProposalAdoptionCoordinator(); // no gate
        AdoptionDecision d = coord.evaluate(proposal(ProposalType.PROMPT, 0.99), 0.99, "c1");
        assertThat(d.getVerdict()).isEqualTo(AdoptionVerdict.REJECTED_FOR_REVIEW);
        assertThat(d.getReason()).contains("no adoption gate");
    }

    @Test
    void evaluatesAllProposalsInAReflectionResult() {
        ProposalAdoptionCoordinator coord = new ProposalAdoptionCoordinator(new CapturingGate());
        ReflectionResult reflection = new ReflectionResult(List.of(
                proposal(ProposalType.PROMPT, 0.95),
                proposal(ProposalType.SCENARIO, 0.40))); // second below confidence
        List<AdoptionDecision> decisions = coord.evaluateAll(reflection, 0.90, "c1");
        assertThat(decisions).hasSize(2);
        assertThat(decisions.get(0).getVerdict()).isEqualTo(AdoptionVerdict.ADMITTED);
        assertThat(decisions.get(1).getVerdict()).isEqualTo(AdoptionVerdict.REJECTED_FOR_REVIEW);
    }
}
