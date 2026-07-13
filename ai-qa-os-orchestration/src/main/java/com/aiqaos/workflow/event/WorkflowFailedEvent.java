package com.aiqaos.workflow.event;

import com.aiqaos.core.event.WorkflowEvent;
import java.util.UUID;

public class WorkflowFailedEvent extends WorkflowEvent {
    public WorkflowFailedEvent(UUID workflowId, String reason) {
        getMetadata().setWorkflowId(workflowId);
        setTransitionState("FAILED: " + reason);
    }
}