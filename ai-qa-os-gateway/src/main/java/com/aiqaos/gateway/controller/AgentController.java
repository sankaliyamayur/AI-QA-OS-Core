package com.aiqaos.gateway.controller;

import com.aiqaos.gateway.dto.AgentResponseDTO;
import com.aiqaos.gateway.dto.AgentStartRequestDTO;
import com.aiqaos.gateway.service.AgentGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final AgentGatewayService agentGatewayService;

    public AgentController(AgentGatewayService agentGatewayService) {
        this.agentGatewayService = agentGatewayService;
    }

    @PostMapping("/start")
    public ResponseEntity<AgentResponseDTO> start(@RequestBody AgentStartRequestDTO request) {
        return ResponseEntity.ok(agentGatewayService.startAgent(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentResponseDTO> getStatus(@PathVariable String id) {
        return ResponseEntity.ok(agentGatewayService.getStatus(id));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<AgentResponseDTO> stop(@PathVariable String id) {
        return ResponseEntity.ok(agentGatewayService.stop(id));
    }
}