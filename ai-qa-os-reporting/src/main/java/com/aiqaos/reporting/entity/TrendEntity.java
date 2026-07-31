package com.aiqaos.reporting.entity;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import org.hibernate.annotations.TenantId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "reporting_trends")
public class TrendEntity extends BaseEntity implements Tenanted {

    // FI-ENT1-C ext: tenant discriminator (ADR-054/057) — Hibernate stamps on insert, filters on read.
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    @Column(name = "trend_date", nullable = false)
    private LocalDate date;

    @Column(name = "pass_rate")
    private double passRate;

    @Column(name = "failure_rate")
    private double failureRate;

    @Column(name = "flaky_rate")
    private double flakyRate;

    @Column(name = "average_duration")
    private long averageDuration;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }
    public double getFailureRate() { return failureRate; }
    public void setFailureRate(double failureRate) { this.failureRate = failureRate; }
    public double getFlakyRate() { return flakyRate; }
    public void setFlakyRate(double flakyRate) { this.flakyRate = flakyRate; }
    public long getAverageDuration() { return averageDuration; }
    public void setAverageDuration(long averageDuration) { this.averageDuration = averageDuration; }
}