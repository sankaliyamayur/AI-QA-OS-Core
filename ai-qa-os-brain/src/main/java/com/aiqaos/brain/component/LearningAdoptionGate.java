package com.aiqaos.brain.component;

import com.aiqaos.core.contract.AdoptionCandidate;
import com.aiqaos.core.contract.AdoptionDecision;
import com.aiqaos.core.contract.ConfidenceDecisionContext;
import com.aiqaos.core.contract.ConfidenceGate;
import com.aiqaos.core.contract.ConfidenceVerdict;
import com.aiqaos.core.contract.SafeAdoptionGate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * LRN-4 — the Brain-owned safe-adoption gate (Rule 2 / ADR-001). A learned improvement is
 * <b>admitted</b> only if:
 * <ol>
 *   <li>its evaluation score clears {@code aiqaos.brain.learning.eval-threshold} (default 0.75), and</li>
 *   <li>its confidence <b>clears the reused AI-1 {@link ConfidenceGate}</b> — verdict {@code PROCEED}
 *       or {@code PROCEED_WITH_VALIDATION}.</li>
 * </ol>
 * Every other outcome is {@code REJECTED_FOR_REVIEW} and logged (never silently dropped).
 *
 * <p><b>Fail-safe nuance:</b> unreported confidence ({@code UNGATED}, {@code c ≤ 0}) is <b>rejected</b>
 * here — the opposite of AI-1's pipeline safeguard (which treats unreported as {@code UNGATED} to
 * avoid halting a run). Adopting a self-modification must be positively trusted, so the absence of a
 * confidence signal must not admit a change.
 */
@Component
public class LearningAdoptionGate implements SafeAdoptionGate {

    private static final Logger log = LoggerFactory.getLogger(LearningAdoptionGate.class);

    private final ConfidenceGate confidenceGate;
    private final double evalThreshold;

    public LearningAdoptionGate(
            ConfidenceGate confidenceGate,
            @Value("${aiqaos.brain.learning.eval-threshold:0.75}") double evalThreshold) {
        this.confidenceGate = confidenceGate;
        this.evalThreshold = evalThreshold;
    }

    @Override
    public AdoptionDecision evaluate(AdoptionCandidate candidate) {
        if (candidate == null) {
            return AdoptionDecision.rejected(null, ConfidenceVerdict.UNGATED, "null candidate");
        }

        boolean evalPass = candidate.getEvalScore() >= evalThreshold;

        ConfidenceVerdict confidenceVerdict = confidenceGate.evaluate(new ConfidenceDecisionContext(
                "learning-adoption:" + candidate.getKind(),
                candidate.getConfidence(),
                candidate.getCorrelationId()));
        boolean confidencePass = confidenceVerdict == ConfidenceVerdict.PROCEED
                || confidenceVerdict == ConfidenceVerdict.PROCEED_WITH_VALIDATION;

        if (evalPass && confidencePass) {
            String reason = String.format("eval %.2f ≥ %.2f and confidence %s",
                    candidate.getEvalScore(), evalThreshold, confidenceVerdict);
            return AdoptionDecision.admitted(candidate.getCandidateId(), confidenceVerdict, reason);
        }

        String reason = buildRejectReason(candidate, evalPass, confidenceVerdict);
        log.warn("[SafeAdoptionGate] REJECTED {} ({}) — {} — logged for human review",
                candidate.getCandidateId(), candidate.getKind(), reason);
        return AdoptionDecision.rejected(candidate.getCandidateId(), confidenceVerdict, reason);
    }

    private String buildRejectReason(AdoptionCandidate c, boolean evalPass, ConfidenceVerdict verdict) {
        StringBuilder sb = new StringBuilder();
        if (!evalPass) {
            sb.append(String.format("eval %.2f < %.2f", c.getEvalScore(), evalThreshold));
        }
        if (verdict != ConfidenceVerdict.PROCEED && verdict != ConfidenceVerdict.PROCEED_WITH_VALIDATION) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(verdict == ConfidenceVerdict.UNGATED
                    ? "confidence not reported (fail-safe)"
                    : "confidence " + verdict);
        }
        return sb.toString();
    }
}
