package com.aiqaos.reporting.entity;

import com.aiqaos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
public class ReportEntity extends BaseEntity {

    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    @Column(name = "execution_id", nullable = false)
    private UUID executionId;

    @Column(name = "report_name", nullable = false)
    private String reportName;

    @Column(name = "report_type", nullable = false)
    private String reportType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "report_version")
    private String reportVersion;

    @Column(name = "quality_score")
    private double qualityScore;

    @Column(name = "automation_score")
    private double automationScore;

    @Column(name = "risk_score")
    private double riskScore;

    @Column(name = "generated_time")
    private LocalDateTime generatedTime;

    public UUID getReportId() { return reportId; }
    public void setReportId(UUID reportId) { this.reportId = reportId; }
    public UUID getExecutionId() { return executionId; }
    public void setExecutionId(UUID executionId) { this.executionId = executionId; }
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReportVersion() { return reportVersion; }
    public void setReportVersion(String reportVersion) { this.reportVersion = reportVersion; }
    public double getQualityScore() { return qualityScore; }
    public void setQualityScore(double qualityScore) { this.qualityScore = qualityScore; }
    public double getAutomationScore() { return automationScore; }
    public void setAutomationScore(double automationScore) { this.automationScore = automationScore; }
    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
    public LocalDateTime getGeneratedTime() { return generatedTime; }
    public void setGeneratedTime(LocalDateTime generatedTime) { this.generatedTime = generatedTime; }
}