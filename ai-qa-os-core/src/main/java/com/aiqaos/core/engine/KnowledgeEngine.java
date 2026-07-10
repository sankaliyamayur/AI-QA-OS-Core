package com.aiqaos.core.engine;

import com.aiqaos.core.contract.BaseRequest;
import com.aiqaos.core.contract.BaseResponse;

public interface KnowledgeEngine<I extends BaseRequest, O extends BaseResponse> {
    O queryKnowledge(I request);
}