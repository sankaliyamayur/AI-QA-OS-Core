package com.aiqaos.core.engine;

import com.aiqaos.core.contract.BaseRequest;
import com.aiqaos.core.contract.BaseResponse;
import com.aiqaos.core.context.WorkflowContext;
import java.util.UUID;

public interface WorkflowEngine<I extends BaseRequest, O extends BaseResponse> {
    O executeWorkflow(I request, WorkflowContext context);
    void cancelWorkflow(UUID workflowId);
}