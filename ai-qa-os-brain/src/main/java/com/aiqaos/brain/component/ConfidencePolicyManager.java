package com.aiqaos.brain.component;

import com.aiqaos.brain.entity.DecisionEntity;
import com.aiqaos.brain.repository.DecisionRepository;
import com.aiqaos.core.contract.ConfidenceDecisionContext;
import com.aiqaos.core.contract.ConfidenceGate;
import com.aiqaos.core.contract.ConfidenceVerdict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI-1 — the Brain-owned confidence gate (Rule 2 / ADR-001).
 *
 * <p>Verdicts by reported confidence {@code c}:
 * <ul>
 *   <li>{@code c <= 0} → {@link ConfidenceVerdict#UNGATED} — not reported (e.g. {@code QAAnalystAgent}'s
 *       {@code 0.0} placeholder); the gate must never halt a run on an unreported value.</li>
 *   <li>{@code c >= high} → {@link ConfidenceVerdict#PROCEED}.</li>
 *   <li>{@code medium <= c < high} → {@link ConfidenceVerdict#PROCEED_WITH_VALIDATION}.</li>
 *   <li>{@code c < medium} → {@link ConfidenceVerdict#HUMAN_REVIEW}.</li>
 * </ul>
 *
 * <p>Thresholds are configuration-driven; each gated decision is persisted as a {@link DecisionEntity}
 * for audit (best-effort — persistence failures never affect the verdict).
 */
@Component
public class ConfidencePolicyManager implements ConfidenceGate {

    private static final Logger log = LoggerFactory.getLogger(ConfidencePolicyManager.class);

    private final ObjectProvider<DecisionRepository> decisionRepositoryProvider;
    private final double high;
    private final double medium;

    public ConfidencePolicyManager(
            ObjectProvider<DecisionRepository> decisionRepositoryProvider,
            @Value("${aiqaos.brain.confidence.high:0.90}") double high,
            @Value("${aiqaos.brain.confidence.medium:0.70}") double medium) {
        this.decisionRepositoryProvider = decisionRepositoryProvider;
        this.high = high;
        this.medium = medium;
    }

    @Override
    public ConfidenceVerdict evaluate(ConfidenceDecisionContext context) {
        double c = context.getConfidence();

        // AI-1 safeguard: unreported confidence (<= 0) is not gated, so runs are never halted on a
        // placeholder value (e.g. QAAnalystAgent currently reports 0.0).
        if (c <= 0.0) {
            log.debug("[ConfidenceGate] {} confidence not reported ({}); UNGATED", context.getStepName(), c);
            return ConfidenceVerdict.UNGATED;
        }

        ConfidenceVerdict verdict;
        if (c >= high) {
            verdict = ConfidenceVerdict.PROCEED;
        } else if (c >= medium) {
            verdict = ConfidenceVerdict.PROCEED_WITH_VALIDATION;
        } else {
            verdict = ConfidenceVerdict.HUMAN_REVIEW;
        }

        record(context, verdict);
        log.info("[ConfidenceGate] {} confidence={} -> {}", context.getStepName(), c, verdict);
        return verdict;
    }

    private void record(ConfidenceDecisionContext context, ConfidenceVerdict verdict) {
        try {
            DecisionRepository repo = decisionRepositoryProvider.getIfAvailable();
            if (repo == null) {
                return;
            }
            DecisionEntity entity = new DecisionEntity();
            entity.setDecisionId(UUID.randomUUID());
            entity.setUserInput("confidence-gate:" + context.getStepName()
                    + (context.getCorrelationId() != null ? " corr=" + context.getCorrelationId() : ""));
            entity.setDecision(verdict.name());
            entity.setConfidence(context.getConfidence());
            entity.setTimestamp(LocalDateTime.now());
            repo.save(entity);
        } catch (Exception ex) {
            // Auditing must never break the gate decision.
            log.warn("[ConfidenceGate] failed to persist decision for {}: {}", context.getStepName(), ex.getMessage());
        }
    }
}
