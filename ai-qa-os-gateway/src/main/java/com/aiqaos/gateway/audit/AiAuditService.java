package com.aiqaos.gateway.audit;

import com.aiqaos.observability.entity.AgentTraceEntity;
import com.aiqaos.observability.entity.LLMCostEntity;
import com.aiqaos.observability.repository.AgentTraceRepository;
import com.aiqaos.observability.repository.LLMCostRepository;
import com.aiqaos.orchestration.entity.HumanReviewEntity;
import com.aiqaos.orchestration.repository.HumanReviewRepository;
import com.aiqaos.security.audit.SecurityAuditEntity;
import com.aiqaos.security.audit.SecurityAuditRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * GOV-1: assembles the unified AI audit trail for a run by reading the run-keyed immutable sources —
 * approvals + security events by {@code workflowId}, model calls + cost by {@code correlationId} —
 * and handing them to {@link AiAuditAssembler}. Read-only; no new writes or schema (Option A).
 *
 * <p>Scope note: {@code DecisionEntity} and {@code PromptExecutionEntity} carry no run key today, so
 * those two facets are excluded until they do (FI-GOV1-D). The gateway is the only app depending on
 * all four sources, hence the placement here.
 */
@Service
public class AiAuditService {

    private static final Logger log = LoggerFactory.getLogger(AiAuditService.class);

    private final HumanReviewRepository reviewRepository;
    private final SecurityAuditRepository securityAuditRepository;
    private final AgentTraceRepository agentTraceRepository;
    private final LLMCostRepository costRepository;
    private final AiAuditAssembler assembler;

    public AiAuditService(HumanReviewRepository reviewRepository,
                          SecurityAuditRepository securityAuditRepository,
                          AgentTraceRepository agentTraceRepository,
                          LLMCostRepository costRepository,
                          AiAuditAssembler assembler) {
        this.reviewRepository = reviewRepository;
        this.securityAuditRepository = securityAuditRepository;
        this.agentTraceRepository = agentTraceRepository;
        this.costRepository = costRepository;
        this.assembler = assembler;
    }

    public AiAuditTrail trailFor(String workflowId, String correlationId) {
        List<AiAuditEvent> events = new ArrayList<>();

        // workflowId-keyed sources: approvals + security events
        if (workflowId != null && !workflowId.isBlank()) {
            try {
                UUID wf = UUID.fromString(workflowId);
                reviewRepository.findByWorkflowId(wf).forEach(r -> events.add(toApproval(r)));
            } catch (IllegalArgumentException e) {
                log.warn("[audit] workflowId '{}' is not a UUID — skipping approvals", workflowId);
            }
            securityAuditRepository.findByWorkflowId(workflowId).forEach(s -> events.add(toSecurity(s)));
        }

        // correlationId-keyed sources: model calls + cost
        if (correlationId != null && !correlationId.isBlank()) {
            agentTraceRepository.findByCorrelationId(correlationId).forEach(t -> events.add(toModelCall(t)));
            costRepository.findByRequestId(correlationId).forEach(c -> events.add(toCost(c)));
        }

        return assembler.assemble(workflowId, correlationId, events);
    }

    private AiAuditEvent toApproval(HumanReviewEntity r) {
        Map<String, Object> details = new HashMap<>();
        details.put("reviewId", str(r.getReviewId()));
        details.put("step", nz(r.getStepName()));
        details.put("status", nz(r.getStatus()));
        details.put("reviewer", nz(r.getReviewer()));
        details.put("comment", nz(r.getDecisionComment()));
        return new AiAuditEvent(AiAuditEvent.Facet.APPROVAL,
                r.getDecidedTime() != null ? r.getDecidedTime() : r.getCreatedTime(),
                nz(r.getReviewer()),
                "Review " + nz(r.getStatus()) + " for step " + nz(r.getStepName()),
                null, details);
    }

    private AiAuditEvent toSecurity(SecurityAuditEntity s) {
        Map<String, Object> details = new HashMap<>();
        details.put("action", nz(s.getAction()));
        details.put("result", nz(s.getResult()));
        details.put("agentId", nz(s.getAgentId()));
        return new AiAuditEvent(AiAuditEvent.Facet.SECURITY, s.getEventTimestamp(),
                nz(s.getUserId()), nz(s.getAction()) + " -> " + nz(s.getResult()), null, details);
    }

    private AiAuditEvent toModelCall(AgentTraceEntity t) {
        Map<String, Object> details = new HashMap<>();
        details.put("provider", nz(t.getProvider()));
        details.put("model", nz(t.getModel()));
        return new AiAuditEvent(AiAuditEvent.Facet.MODEL_CALL, t.getTimestamp(),
                nz(t.getAgentType()), nz(t.getProvider()) + "/" + nz(t.getModel()), null, details);
    }

    private AiAuditEvent toCost(LLMCostEntity c) {
        Map<String, Object> details = new HashMap<>();
        details.put("provider", nz(c.getProvider()));
        details.put("model", nz(c.getModel()));
        details.put("cost", c.getCost());
        return new AiAuditEvent(AiAuditEvent.Facet.COST, c.getTimestamp(),
                nz(c.getAgentType()), "$" + c.getCost() + " (" + nz(c.getModel()) + ")", c.getCost(), details);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
