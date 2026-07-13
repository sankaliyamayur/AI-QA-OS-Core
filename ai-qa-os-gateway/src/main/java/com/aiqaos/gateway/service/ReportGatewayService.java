package com.aiqaos.gateway.service;

import com.aiqaos.gateway.client.ReportingClient;
import com.aiqaos.gateway.dto.GatewayRequestDTO;
import com.aiqaos.gateway.dto.ReportResponseDTO;
import com.aiqaos.gateway.event.GatewayEventPublisher;
import com.aiqaos.security.ratelimit.RateLimiter;
import org.springframework.stereotype.Service;

@Service
public class ReportGatewayService extends GatewayService {

    private final ReportingClient reportingClient;

    public ReportGatewayService(ReportingClient reportingClient,
                                 GatewayEventPublisher eventPublisher,
                                 RateLimiter rateLimiter) {
        super(eventPublisher, rateLimiter);
        this.reportingClient = reportingClient;
    }

    public ReportResponseDTO generate(GatewayRequestDTO request) {
        return reportingClient.generate(request);
    }

    public ReportResponseDTO getReport(String reportId) {
        return reportingClient.getReport(reportId);
    }

    public String getExportUrl(String reportId, String format) {
        return reportingClient.getExportUrl(reportId, format);
    }
}