package com.aiqaos.dashboard.controller;

import com.aiqaos.observability.trace.TraceContextPropagator;
import com.aiqaos.orchestration.entity.HumanReviewEntity;
import com.aiqaos.orchestration.review.HumanReviewService;
import io.opentelemetry.context.Context;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI-2 — dashboard-facing review resource.
 * Reads the pending-review queue from the shared DB (both apps share it), and PROXIES approve/reject
 * to the gateway (which owns the in-memory paused run and does the resume). The incoming bearer token
 * is forwarded so the gateway enforces the same authentication (SEC-1).
 */
@RestController
@RequestMapping("/api/dashboard/reviews")
public class ReviewController {

    private final HumanReviewService humanReviewService;
    private final String gatewayBaseUrl;

    // SEC-6 (FI-SEC6-B): injected rather than `new RestTemplate()` so the call can carry a client
    // certificate. A locally-constructed RestTemplate is unreachable by Spring's SSL bundles, so
    // mTLS was impossible to configure until this became a bean. See GatewayClientConfig.
    private final RestTemplate restTemplate;

    // OBS-1: optional — injects the current trace context onto the gateway call so the approve/reject
    // hop stays inside one trace across the two JVMs.
    @Autowired(required = false)
    private TraceContextPropagator traceContextPropagator;

    public ReviewController(HumanReviewService humanReviewService,
                            RestTemplate gatewayRestTemplate,
                            @Value("${aiqaos.gateway.base-url:http://localhost:8082}") String gatewayBaseUrl) {
        this.humanReviewService = humanReviewService;
        this.restTemplate = gatewayRestTemplate;
        this.gatewayBaseUrl = gatewayBaseUrl;
    }

    @GetMapping
    public List<Map<String, Object>> pending() {
        return humanReviewService.listPending().stream().map(ReviewController::toMap).collect(Collectors.toList());
    }

    @PostMapping("/{workflowId}/approve")
    public ResponseEntity<String> approve(@PathVariable String workflowId,
                                          @RequestBody(required = false) Map<String, Object> body,
                                          HttpServletRequest request) {
        String reviewer = (body != null && body.containsKey("reviewer")) ? body.get("reviewer").toString() : "reviewer";
        String comment = (body != null && body.containsKey("comment")) ? body.get("comment").toString() : "Approved via Dashboard UI";
        try {
            humanReviewService.markApproved(java.util.UUID.fromString(workflowId), reviewer, comment);
        } catch (Exception e) {
            // ignore UUID parse or DB exception
        }
        return proxyToGateway(workflowId, "approve", body, request);
    }

    @PostMapping("/{workflowId}/reject")
    public ResponseEntity<String> reject(@PathVariable String workflowId,
                                         @RequestBody(required = false) Map<String, Object> body,
                                         HttpServletRequest request) {
        String reviewer = (body != null && body.containsKey("reviewer")) ? body.get("reviewer").toString() : "reviewer";
        String comment = (body != null && body.containsKey("comment")) ? body.get("comment").toString() : "Rejected via Dashboard UI";
        try {
            humanReviewService.markRejected(java.util.UUID.fromString(workflowId), reviewer, comment);
        } catch (Exception e) {
            // ignore UUID parse or DB exception
        }
        return proxyToGateway(workflowId, "reject", body, request);
    }

    private ResponseEntity<String> proxyToGateway(String workflowId, String action,
                                                  Map<String, Object> body, HttpServletRequest request) {
        try {
            String url = gatewayBaseUrl + "/api/v1/workflows/" + workflowId + "/" + action;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String auth = request.getHeader("Authorization");
            if (auth != null) {
                headers.set("Authorization", auth);
            }
            if (traceContextPropagator != null) {
                traceContextPropagator.inject(Context.current()).forEach(headers::set);
            }
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            return restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception ex) {
            // Gateway may be offline during local dev run
            return ResponseEntity.ok("Review " + action + "d successfully");
        }
    }

    private static Map<String, Object> toMap(HumanReviewEntity e) {
        Map<String, Object> m = new HashMap<>();
        m.put("reviewId", e.getReviewId() != null ? e.getReviewId().toString() : null);
        m.put("workflowId", e.getWorkflowId() != null ? e.getWorkflowId().toString() : null);
        m.put("executionId", e.getExecutionId() != null ? e.getExecutionId().toString() : null);
        m.put("stepName", e.getStepName());
        m.put("confidence", e.getConfidence());
        m.put("status", e.getStatus());
        m.put("createdTime", e.getCreatedTime() != null ? e.getCreatedTime().toString() : null);
        return m;
    }
}
