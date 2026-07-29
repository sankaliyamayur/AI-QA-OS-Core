package com.aiqaos.learning.adoption;

import com.aiqaos.core.contract.AdoptionCandidate;
import com.aiqaos.core.contract.AdoptionDecision;
import com.aiqaos.core.contract.AdoptionKind;
import com.aiqaos.core.contract.AdoptionVerdict;
import com.aiqaos.core.contract.SafeAdoptionGate;
import com.aiqaos.learning.reflection.ImprovementProposal;
import com.aiqaos.learning.reflection.ReflectionResult;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * FI-LRN4-A: connects the learning loop end-to-end — maps an LRN-1 {@link ImprovementProposal} to a
 * core {@link AdoptionCandidate} and runs it through the LRN-4 {@link SafeAdoptionGate}, so a recorded
 * improvement is only admitted for adoption when it passes eval + confidence. The gate contract lives
 * in {@code core}, so {@code learning} uses it without depending on {@code brain}.
 *
 * <p>The gate is optional: when no {@link SafeAdoptionGate} is on the classpath (e.g. a learning-only
 * deployment), proposals are conservatively {@code REJECTED_FOR_REVIEW} — never adopted ungated. The
 * candidate's {@code evalScore} is supplied by the caller (running real eval to produce it is
 * FI-LRN4-B).
 */
@Service
public class ProposalAdoptionCoordinator {

    private static final Logger log = LoggerFactory.getLogger(ProposalAdoptionCoordinator.class);

    @Autowired(required = false)
    private SafeAdoptionGate adoptionGate;

    public ProposalAdoptionCoordinator() {
    }

    /** Test seam: supply the adoption gate explicitly. */
    ProposalAdoptionCoordinator(SafeAdoptionGate adoptionGate) {
        this.adoptionGate = adoptionGate;
    }

    /**
     * Gate a single proposal for adoption. {@code evalScore} is the proposal's measured quality
     * (from MOD-3/PE-1, supplied upstream); the proposal's own confidence is its trust score.
     */
    public AdoptionDecision evaluate(ImprovementProposal proposal, double evalScore, String correlationId) {
        if (proposal == null) {
            return AdoptionDecision.rejected(null, null, "null proposal");
        }
        AdoptionCandidate candidate = new AdoptionCandidate(
                proposal.getProposalId(),
                toAdoptionKind(proposal),
                evalScore,
                proposal.getConfidence(),
                proposal.getRationale(),
                correlationId);

        if (adoptionGate == null) {
            log.warn("[ProposalAdoption] no SafeAdoptionGate present — {} not adopted (fail-safe)",
                    proposal.getProposalId());
            return new AdoptionDecision(proposal.getProposalId(), AdoptionVerdict.REJECTED_FOR_REVIEW,
                    "no adoption gate present (fail-safe)", null);
        }
        return adoptionGate.evaluate(candidate);
    }

    /** Gate every proposal in a reflection result using a uniform {@code evalScore}. */
    public List<AdoptionDecision> evaluateAll(ReflectionResult reflection, double evalScore,
                                              String correlationId) {
        List<AdoptionDecision> decisions = new ArrayList<>();
        if (reflection != null) {
            for (ImprovementProposal p : reflection.getProposals()) {
                decisions.add(evaluate(p, evalScore, correlationId));
            }
        }
        return decisions;
    }

    private AdoptionKind toAdoptionKind(ImprovementProposal proposal) {
        // ProposalType (PROMPT/SCENARIO/AUTOMATION) maps by name to the core AdoptionKind.
        return AdoptionKind.valueOf(proposal.getType().name());
    }
}
