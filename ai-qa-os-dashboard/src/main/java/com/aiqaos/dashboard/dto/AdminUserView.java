package com.aiqaos.dashboard.dto;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ENT-4: an admin-surface view of a user — the fields an administrator needs to see (identity +
 * security posture), without exposing secrets (no password hash / MFA secret).
 *
 * <p>FI-ENT4-A: carries the user {@code id} (the stable PK, so the admin UI can target the write
 * endpoints {@code /api/admin/users/{id}/...}) and the assigned {@code roles} (so the UI can show and
 * pre-fill the role editor). Neither is a secret.
 */
public final class AdminUserView {

    private final UUID id;
    private final String username;
    private final String email;
    private final boolean enabled;
    private final boolean mfaEnabled;
    private final boolean accountLocked;
    private final List<String> roles;

    public AdminUserView(UUID id, String username, String email, boolean enabled, boolean mfaEnabled,
                         boolean accountLocked, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.enabled = enabled;
        this.mfaEnabled = mfaEnabled;
        this.accountLocked = accountLocked;
        this.roles = roles != null ? Collections.unmodifiableList(List.copyOf(roles)) : List.of();
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isEnabled() { return enabled; }
    public boolean isMfaEnabled() { return mfaEnabled; }
    public boolean isAccountLocked() { return accountLocked; }
    public List<String> getRoles() { return roles; }
}
