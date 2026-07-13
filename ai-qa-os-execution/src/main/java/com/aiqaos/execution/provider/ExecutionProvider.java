package com.aiqaos.execution.provider;

import com.aiqaos.core.contract.ExecutionRequest;
import com.aiqaos.core.contract.ExecutionResponse;

public interface ExecutionProvider {
    String getProviderType();
    ExecutionResponse execute(ExecutionRequest request);
}