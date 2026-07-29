package com.aiqaos.gateway.audit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * GOV-1: read-only AI audit trail endpoint. Auth-gated by the SEC-1 chain (only the public
 * allow-list is open). Returns the unified trail of a run's AI actions (approvals, model calls,
 * cost, security events) — pass the run's {@code correlationId} to include the model-call/cost facets.
 */
@RestController
@RequestMapping("/api/v1/audit")
public class AiAuditController {

    private final AiAuditService auditService;

    public AiAuditController(AiAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/{workflowId}")
    public ResponseEntity<AiAuditTrail> trail(@PathVariable String workflowId,
                                              @RequestParam(required = false) String correlationId) {
        return ResponseEntity.ok(auditService.trailFor(workflowId, correlationId));
    }
}
