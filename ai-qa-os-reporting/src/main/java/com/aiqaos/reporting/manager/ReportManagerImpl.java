package com.aiqaos.reporting.manager;

import com.aiqaos.core.contract.ReportRequest;
import com.aiqaos.core.contract.ReportResponse;
import com.aiqaos.core.engine.ReportingEngine;
import org.springframework.stereotype.Service;

@Service
public class ReportManagerImpl implements ReportingEngine<ReportRequest, ReportResponse> {

    @Override
    public ReportResponse generateReport(ReportRequest request) {
        ReportResponse response = new ReportResponse();
        response.getMetadata().setCorrelationId(request.getMetadata().getCorrelationId());
        response.getMetadata().setTraceId(request.getMetadata().getTraceId());
        response.setReportUrl("http://localhost:8080/reports/" + request.getExecutionId());
        response.setStatus("SUCCESS");
        return response;
    }
}