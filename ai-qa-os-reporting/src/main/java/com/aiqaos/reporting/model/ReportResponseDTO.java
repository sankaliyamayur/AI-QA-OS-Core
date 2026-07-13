package com.aiqaos.reporting.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.UUID;

public class ReportResponseDTO implements BaseDTO {
    private UUID reportId;
    private String status;

    public UUID getReportId() { return reportId; }
    public void setReportId(UUID reportId) { this.reportId = reportId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}