package com.aiqaos.workflow.event;

import com.aiqaos.core.event.WorkflowEvent;
import java.util.UUID;

public class WorkflowStepCompletedEvent extends WorkflowEvent {
    public WorkflowStepCompletedEvent(UUID workflowId, String stepName) {
        getMetadata().setWorkflowId(workflowId);
        setWorkflowName(stepName);
        setTransitionState("STEP_COMPLETED");
    }
}