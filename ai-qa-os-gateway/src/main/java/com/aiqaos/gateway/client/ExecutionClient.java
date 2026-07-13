package com.aiqaos.gateway.client;

import com.aiqaos.gateway.dto.ExecutionRequestDTO;
import com.aiqaos.gateway.dto.ExecutionResponseDTO;

public interface ExecutionClient {
    ExecutionResponseDTO run(ExecutionRequestDTO request);
    ExecutionResponseDTO getStatus(String executionId);
    ExecutionResponseDTO cancel(String executionId);
    String getArtifactsUrl(String executionId);
}