package com.aiqaos.core.engine;

import com.aiqaos.core.contract.BaseRequest;
import com.aiqaos.core.contract.BaseResponse;

public interface ValidationEngine<I extends BaseRequest, O extends BaseResponse> {
    O validate(I request);
}