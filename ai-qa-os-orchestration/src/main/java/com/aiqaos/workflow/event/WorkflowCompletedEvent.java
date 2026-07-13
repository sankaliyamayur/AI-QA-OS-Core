package com.aiqaos.workflow.event;

import com.aiqaos.core.event.WorkflowEvent;
import java.util.UUID;

public class WorkflowCompletedEvent extends WorkflowEvent {
    public WorkflowCompletedEvent(UUID workflowId) {
        getMetadata().setWorkflowId(workflowId);
        setTransitionState("COMPLETED");
    }
}