package com.aiqaos.core.engine;

import com.aiqaos.core.contract.BaseRequest;
import com.aiqaos.core.contract.BaseResponse;
import com.aiqaos.core.context.AgentContext;
import com.aiqaos.core.enums.AgentStatus;
import com.aiqaos.core.enums.AgentType;

public interface Agent<I extends BaseRequest, O extends BaseResponse> {
    O execute(I request, AgentContext context);
    AgentStatus getStatus();
    AgentType getType();
}