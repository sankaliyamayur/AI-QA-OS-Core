package com.aiqaos.execution.component;

import com.aiqaos.core.contract.ExecutionRequest;
import com.aiqaos.core.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ExecutionValidator {
    public void validate(ExecutionRequest request) {
        if (request.getScriptContent() == null || request.getScriptContent().trim().isEmpty()) {
            throw new ValidationException("Script content cannot be null or empty.");
        }
    }
}