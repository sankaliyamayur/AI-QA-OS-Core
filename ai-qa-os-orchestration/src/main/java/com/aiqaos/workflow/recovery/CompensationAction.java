package com.aiqaos.workflow.recovery;

import com.aiqaos.core.context.WorkflowContext;

public interface CompensationAction {
    void rollback(WorkflowContext context);
}