package com.aiqaos.dashboard.dto;

/**
 * ENT-4: an admin-surface view of a user — the fields an administrator needs to see (identity +
 * security posture), without exposing secrets (no password hash / MFA secret).
 */
public final class AdminUserView {

    private final String username;
    private final String email;
    private final boolean enabled;
    private final boolean mfaEnabled;
    private final boolean accountLocked;

    public AdminUserView(String username, String email, boolean enabled, boolean mfaEnabled,
                         boolean accountLocked) {
        this.username = username;
        this.email = email;
        this.enabled = enabled;
        this.mfaEnabled = mfaEnabled;
        this.accountLocked = accountLocked;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public boolean isEnabled() { return enabled; }
    public boolean isMfaEnabled() { return mfaEnabled; }
    public boolean isAccountLocked() { return accountLocked; }
}
