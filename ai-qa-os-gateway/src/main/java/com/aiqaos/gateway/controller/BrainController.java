package com.aiqaos.gateway.controller;

import com.aiqaos.gateway.dto.BrainRequestDTO;
import com.aiqaos.gateway.dto.GatewayResponseDTO;
import com.aiqaos.gateway.service.BrainGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/brain")
public class BrainController {

    private final BrainGatewayService brainGatewayService;

    public BrainController(BrainGatewayService brainGatewayService) {
        this.brainGatewayService = brainGatewayService;
    }

    @PostMapping("/request")
    public ResponseEntity<GatewayResponseDTO> submitRequest(@RequestBody BrainRequestDTO request) {
        return ResponseEntity.ok(brainGatewayService.submitRequest(request));
    }

    @GetMapping("/decision/{id}")
    public ResponseEntity<GatewayResponseDTO> getDecision(@PathVariable String id) {
        return ResponseEntity.ok(brainGatewayService.getDecision(id));
    }
}