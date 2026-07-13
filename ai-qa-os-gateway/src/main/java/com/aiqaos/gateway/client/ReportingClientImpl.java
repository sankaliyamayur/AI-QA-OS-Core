package com.aiqaos.gateway.client;

import com.aiqaos.gateway.dto.GatewayRequestDTO;
import com.aiqaos.gateway.dto.ReportResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ReportingClientImpl implements ReportingClient {

    @Override
    public ReportResponseDTO generate(GatewayRequestDTO request) {
        // TODO: delegate to ReportManager
        return new ReportResponseDTO();
    }

    @Override public ReportResponseDTO getReport(String reportId) { return new ReportResponseDTO(); }
    @Override public String getExportUrl(String reportId, String format) { return "/reports/" + reportId + "/export?format=" + format; }
}