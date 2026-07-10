package com.aiqaos.core.engine;

import com.aiqaos.core.contract.BaseRequest;
import com.aiqaos.core.contract.BaseResponse;
import com.aiqaos.core.context.WorkflowContext;

public interface WorkflowStep<I extends BaseRequest, O extends BaseResponse> {
    O execute(I request, WorkflowContext context);
    String getName();
}