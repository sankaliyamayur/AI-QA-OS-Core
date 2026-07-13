package com.aiqaos.gateway.service;

import com.aiqaos.gateway.client.BrainClient;
import com.aiqaos.gateway.dto.BrainRequestDTO;
import com.aiqaos.gateway.dto.GatewayResponseDTO;
import com.aiqaos.gateway.event.GatewayEventPublisher;
import com.aiqaos.security.ratelimit.RateLimiter;
import org.springframework.stereotype.Service;

@Service
public class BrainGatewayService extends GatewayService {

    private final BrainClient brainClient;

    public BrainGatewayService(BrainClient brainClient,
                                GatewayEventPublisher eventPublisher,
                                RateLimiter rateLimiter) {
        super(eventPublisher, rateLimiter);
        this.brainClient = brainClient;
    }

    public GatewayResponseDTO submitRequest(BrainRequestDTO request) {
        log.info("Brain request: correlationId={}, type={}", request.getCorrelationId(), request.getRequestType());
        return brainClient.analyze(request);
    }

    public GatewayResponseDTO getDecision(String decisionId) {
        return brainClient.getDecision(decisionId);
    }
}