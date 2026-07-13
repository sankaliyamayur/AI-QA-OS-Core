package com.aiqaos.gateway.client;

import com.aiqaos.gateway.dto.GatewayRequestDTO;
import com.aiqaos.gateway.dto.ReportResponseDTO;

public interface ReportingClient {
    ReportResponseDTO generate(GatewayRequestDTO request);
    ReportResponseDTO getReport(String reportId);
    String getExportUrl(String reportId, String format);
}