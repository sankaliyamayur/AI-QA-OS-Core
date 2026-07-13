package com.aiqaos.gateway.dto;

public class ReportResponseDTO extends GatewayResponseDTO {
    private String reportId;
    private String reportUrl;
    private String summary;

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getReportUrl() { return reportUrl; }
    public void setReportUrl(String reportUrl) { this.reportUrl = reportUrl; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}