package com.aiqaos.security.rbac;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "security_password_history")
public class PasswordHistoryEntity extends BaseEntity implements Tenanted {

    @TenantId
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "changed_date", nullable = false)
    private LocalDateTime changedDate;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public LocalDateTime getChangedDate() { return changedDate; }
    public void setChangedDate(LocalDateTime changedDate) { this.changedDate = changedDate; }
    @Override public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
