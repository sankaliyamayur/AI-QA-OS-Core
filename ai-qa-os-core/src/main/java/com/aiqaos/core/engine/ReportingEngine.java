package com.aiqaos.core.engine;

import com.aiqaos.core.contract.BaseRequest;
import com.aiqaos.core.contract.BaseResponse;

public interface ReportingEngine<I extends BaseRequest, O extends BaseResponse> {
    O generateReport(I request);
}