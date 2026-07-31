package com.aiqaos.security.admin;

import com.aiqaos.security.rbac.UserEntity;
import java.util.List;
import java.util.UUID;

/**
 * FI-ENT4-A (ADR-067): secret-free projection of a {@link UserEntity} returned by the admin write
 * API. Deliberately omits the password hash and MFA secret — an admin surface never echoes secrets.
 *
 * <p>The dashboard read-model has its own equivalent DTO ({@code dashboard.dto.AdminUserView}); this
 * is a security-local copy so the security module does not depend on the dashboard (wrong direction).
 */
public record AdminUserView(
        UUID id,
        String username,
        String email,
        boolean enabled,
        boolean accountLocked,
        List<String> roles) {

    public static AdminUserView from(UserEntity user) {
        return new AdminUserView(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.isAccountLocked(),
                List.copyOf(user.getRoles()));
    }
}
