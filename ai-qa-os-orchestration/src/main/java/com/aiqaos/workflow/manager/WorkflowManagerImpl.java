package com.aiqaos.workflow.manager;

import com.aiqaos.core.engine.WorkflowEngine;
import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.workflow.executor.WorkflowExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class WorkflowManagerImpl implements WorkflowEngine<WorkflowRequest, WorkflowResponse> {

    @Autowired
    private WorkflowExecutor workflowExecutor;

    @Override
    public WorkflowResponse executeWorkflow(WorkflowRequest request, WorkflowContext context) {
        return workflowExecutor.execute(request, context);
    }

    @Override
    public void cancelWorkflow(UUID workflowId) {
        // Cancellation logic details placeholder
    }
}