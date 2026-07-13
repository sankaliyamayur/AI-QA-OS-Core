package com.aiqaos.gateway.client;

import com.aiqaos.gateway.dto.ExecutionRequestDTO;
import com.aiqaos.gateway.dto.ExecutionResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ExecutionClientImpl implements ExecutionClient {

    @Override
    public ExecutionResponseDTO run(ExecutionRequestDTO request) {
        // TODO: delegate to ExecutionManager
        ExecutionResponseDTO r = new ExecutionResponseDTO();
        r.setExecutionStatus("RUNNING");
        return r;
    }

    @Override public ExecutionResponseDTO getStatus(String executionId) { return new ExecutionResponseDTO(); }
    @Override public ExecutionResponseDTO cancel(String executionId)    { return new ExecutionResponseDTO(); }
    @Override public String getArtifactsUrl(String executionId)        { return "/artifacts/" + executionId; }
}