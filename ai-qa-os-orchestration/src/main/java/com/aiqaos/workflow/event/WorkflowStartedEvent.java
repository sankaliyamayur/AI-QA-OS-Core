package com.aiqaos.workflow.event;

import com.aiqaos.core.event.WorkflowEvent;
import java.util.UUID;

public class WorkflowStartedEvent extends WorkflowEvent {
    public WorkflowStartedEvent(UUID workflowId) {
        getMetadata().setWorkflowId(workflowId);
        setTransitionState("STARTED");
    }
}