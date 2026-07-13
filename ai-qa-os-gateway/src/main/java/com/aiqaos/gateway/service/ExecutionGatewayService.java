package com.aiqaos.gateway.service;

import com.aiqaos.gateway.client.ExecutionClient;
import com.aiqaos.gateway.dto.ExecutionRequestDTO;
import com.aiqaos.gateway.dto.ExecutionResponseDTO;
import com.aiqaos.gateway.event.GatewayEventPublisher;
import com.aiqaos.security.ratelimit.RateLimiter;
import org.springframework.stereotype.Service;

@Service
public class ExecutionGatewayService extends GatewayService {

    private final ExecutionClient executionClient;

    public ExecutionGatewayService(ExecutionClient executionClient,
                                    GatewayEventPublisher eventPublisher,
                                    RateLimiter rateLimiter) {
        super(eventPublisher, rateLimiter);
        this.executionClient = executionClient;
    }

    public ExecutionResponseDTO run(ExecutionRequestDTO request) {
        log.info("Execution run: workflowId={}", request.getWorkflowId());
        return executionClient.run(request);
    }

    public ExecutionResponseDTO getStatus(String executionId)  { return executionClient.getStatus(executionId); }
    public ExecutionResponseDTO cancel(String executionId)     { return executionClient.cancel(executionId); }
    public String getArtifactsUrl(String executionId)          { return executionClient.getArtifactsUrl(executionId); }
}