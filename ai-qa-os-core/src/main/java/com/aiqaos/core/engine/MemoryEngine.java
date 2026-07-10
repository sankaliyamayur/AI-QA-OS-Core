package com.aiqaos.core.engine;

import com.aiqaos.core.contract.BaseRequest;
import com.aiqaos.core.contract.BaseResponse;

public interface MemoryEngine<I extends BaseRequest, O extends BaseResponse> {
    O queryMemory(I request);
    void storeMemory(I request, O response);
}