package com.aiqaos.gateway.client;

import com.aiqaos.gateway.dto.BrainRequestDTO;
import com.aiqaos.gateway.dto.GatewayResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class BrainClientImpl implements BrainClient {

    @Override
    public GatewayResponseDTO analyze(BrainRequestDTO request) {
        // TODO: delegate to BrainManagerImpl
        return GatewayResponseDTO.success(request.getCorrelationId(), null);
    }

    @Override
    public GatewayResponseDTO getDecision(String decisionId) {
        return GatewayResponseDTO.success(null, null);
    }
}