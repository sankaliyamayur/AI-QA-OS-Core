package com.aiqaos.execution.dispatcher;

import com.aiqaos.core.contract.ExecutionRequest;
import com.aiqaos.core.contract.ExecutionResponse;
import com.aiqaos.execution.provider.ExecutionProvider;
import com.aiqaos.execution.provider.ExecutionProviderRegistry;
import com.aiqaos.core.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExecutionDispatcher {

    @Autowired
    private ExecutionProviderRegistry providerRegistry;

    public ExecutionResponse dispatch(ExecutionRequest request) {
        String targetProvider = request.getRuntimeLanguage() != null ? request.getRuntimeLanguage() : "Selenium";
        ExecutionProvider provider = providerRegistry.getProvider(targetProvider);
        if (provider == null) {
            throw new ValidationException("Unsupported execution provider: " + targetProvider);
        }
        return provider.execute(request);
    }
}