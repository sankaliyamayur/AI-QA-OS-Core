package com.aiqaos.gateway.service;

import com.aiqaos.gateway.dto.AgentResponseDTO;
import com.aiqaos.gateway.dto.AgentStartRequestDTO;
import com.aiqaos.gateway.event.GatewayEventPublisher;
import com.aiqaos.security.ratelimit.RateLimiter;
import org.springframework.stereotype.Service;

@Service
public class AgentGatewayService extends GatewayService {

    public AgentGatewayService(GatewayEventPublisher eventPublisher, RateLimiter rateLimiter) {
        super(eventPublisher, rateLimiter);
    }

    public AgentResponseDTO startAgent(AgentStartRequestDTO request) {
        log.info("Starting agent: type={}", request.getAgentType());
        AgentResponseDTO r = new AgentResponseDTO();
        r.setAgentType(request.getAgentType());
        r.setCurrentTask(request.getTask());
        return r;
    }

    public AgentResponseDTO getStatus(String agentId) {
        return new AgentResponseDTO();
    }

    public AgentResponseDTO stop(String agentId) {
        return new AgentResponseDTO();
    }
}