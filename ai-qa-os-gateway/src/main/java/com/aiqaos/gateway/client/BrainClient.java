package com.aiqaos.gateway.client;

import com.aiqaos.gateway.dto.BrainRequestDTO;
import com.aiqaos.gateway.dto.GatewayResponseDTO;

public interface BrainClient {
    GatewayResponseDTO analyze(BrainRequestDTO request);
    GatewayResponseDTO getDecision(String decisionId);
}