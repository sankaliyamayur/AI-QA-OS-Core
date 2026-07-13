package com.aiqaos.gateway.controller;

import com.aiqaos.gateway.dto.ExecutionRequestDTO;
import com.aiqaos.gateway.dto.ExecutionResponseDTO;
import com.aiqaos.gateway.service.ExecutionGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/execution")
public class ExecutionController {

    private final ExecutionGatewayService executionGatewayService;

    public ExecutionController(ExecutionGatewayService executionGatewayService) {
        this.executionGatewayService = executionGatewayService;
    }

    @PostMapping("/run")
    public ResponseEntity<ExecutionResponseDTO> run(@RequestBody ExecutionRequestDTO request) {
        return ResponseEntity.ok(executionGatewayService.run(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionResponseDTO> getStatus(@PathVariable String id) {
        return ResponseEntity.ok(executionGatewayService.getStatus(id));
    }

    @GetMapping("/{id}/artifacts")
    public ResponseEntity<String> getArtifacts(@PathVariable String id) {
        return ResponseEntity.ok(executionGatewayService.getArtifactsUrl(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ExecutionResponseDTO> cancel(@PathVariable String id) {
        return ResponseEntity.ok(executionGatewayService.cancel(id));
    }
}