package com.aiqaos.dashboard.controller;

import com.aiqaos.dashboard.dto.AgentTraceDetailDTO;
import com.aiqaos.dashboard.dto.AgentTraceSummaryDTO;
import com.aiqaos.dashboard.service.AgentTraceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard/agents/traces")
public class AgentTraceController {

    private final AgentTraceService agentTraceService;

    public AgentTraceController(AgentTraceService agentTraceService) {
        this.agentTraceService = agentTraceService;
    }

    @GetMapping
    public List<AgentTraceSummaryDTO> search(@RequestParam(name = "correlationId", required = false) String correlationId,
                                              @RequestParam(name = "executionId", required = false) UUID executionId) {
        if (correlationId != null) {
            return agentTraceService.getByCorrelationId(correlationId);
        }
        if (executionId != null) {
            return agentTraceService.getByExecutionId(executionId);
        }
        return agentTraceService.getRecent();
    }

    @GetMapping("/{id}")
    public AgentTraceDetailDTO getDetail(@PathVariable("id") UUID id) {
        return agentTraceService.getDetail(id);
    }
}
