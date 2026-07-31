package com.aiqaos.reporting.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import org.hibernate.annotations.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "report_artifacts")
public class ReportArtifactEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-C ext: tenant discriminator (ADR-054/057) — Hibernate stamps on insert, filters on read.
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    @Column(name = "html_path")
    private String htmlPath;

    @Column(name = "pdf_path")
    private String pdfPath;

    @Column(name = "json_path")
    private String jsonPath;

    @Column(name = "junit_xml_path")
    private String junitXmlPath;

    @Column(name = "archive_path")
    private String archivePath;

    public UUID getReportId() { return reportId; }
    public void setReportId(UUID reportId) { this.reportId = reportId; }
    public String getHtmlPath() { return htmlPath; }
    public void setHtmlPath(String htmlPath) { this.htmlPath = htmlPath; }
    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
    public String getJsonPath() { return jsonPath; }
    public void setJsonPath(String jsonPath) { this.jsonPath = jsonPath; }
    public String getJunitXmlPath() { return junitXmlPath; }
    public void setJunitXmlPath(String junitXmlPath) { this.junitXmlPath = junitXmlPath; }
    public String getArchivePath() { return archivePath; }
    public void setArchivePath(String archivePath) { this.archivePath = archivePath; }
}