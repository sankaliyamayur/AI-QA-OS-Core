package com.aiqaos.workflow.component;

import com.aiqaos.workflow.dsl.WorkflowDefinition;
import com.aiqaos.core.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class WorkflowValidator {
    public void validate(WorkflowDefinition definition) {
        if (definition.getId() == null || definition.getId().isEmpty()) {
            throw new ValidationException("Workflow validation failed: ID is missing.");
        }
        if (definition.getSteps() == null || definition.getSteps().isEmpty()) {
            throw new ValidationException("Workflow validation failed: steps list is empty.");
        }
    }
}