package com.aiqaos.security.rbac;

import com.aiqaos.core.entity.BaseEntity;
import com.aiqaos.core.tenant.Tenanted;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.TenantId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "security_users", uniqueConstraints = {
        // FI-ENT1-D: identity is unique WITHIN a tenant, not globally (acme/alice ≠ beta/alice).
        @UniqueConstraint(name = "ux_users_tenant_username", columnNames = {"tenant_id", "username"}),
        @UniqueConstraint(name = "ux_users_tenant_email", columnNames = {"tenant_id", "email"})
})
public class UserEntity extends BaseEntity implements Tenanted {

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    // SaaS Multi-Tenancy fields
    // FI-ENT1-C/D: tenant discriminator (ADR-054/055) — String, matching TenantContext; Hibernate
    // stamps it on insert and filters it on read. Reconciled from the earlier UUID column via V18.
    @TenantId
    @Column(name = "tenant_id", length = 64, nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "workspace_id")
    private UUID workspaceId;

    // MFA-ready fields
    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;

    @Column(name = "mfa_secret")
    private String mfaSecret;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "backup_code")
    private List<String> backupCodes = new ArrayList<>();

    // FI-ENT4-C (ADR-066): the user's assigned role names (the global catalog, ADR-055). Authorities are
    // derived as ROLE_<name> at authentication. EAGER so roles are available on load without a round-trip.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "security_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_name")
    private List<String> roles = new ArrayList<>();

    // Account lockout features
    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public UUID getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(UUID workspaceId) { this.workspaceId = workspaceId; }

    public boolean isMfaEnabled() { return mfaEnabled; }
    public void setMfaEnabled(boolean mfaEnabled) { this.mfaEnabled = mfaEnabled; }
    public String getMfaSecret() { return mfaSecret; }
    public void setMfaSecret(String mfaSecret) { this.mfaSecret = mfaSecret; }
    public List<String> getBackupCodes() { return backupCodes; }
    public void setBackupCodes(List<String> backupCodes) { this.backupCodes = backupCodes; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public boolean isAccountLocked() { return accountLocked; }
    public void setAccountLocked(boolean accountLocked) { this.accountLocked = accountLocked; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
}