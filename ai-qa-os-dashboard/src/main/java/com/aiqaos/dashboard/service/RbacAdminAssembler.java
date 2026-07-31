package com.aiqaos.dashboard.service;

import com.aiqaos.dashboard.dto.AdminUserView;
import com.aiqaos.dashboard.dto.RbacAdminSummary;
import com.aiqaos.security.rbac.PermissionEntity;
import com.aiqaos.security.rbac.RoleEntity;
import com.aiqaos.security.rbac.UserEntity;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * ENT-4: composes the existing {@code ai-qa-os-security} RBAC entities into an
 * {@link RbacAdminSummary} for the admin surface — counts, security-posture tallies, and safe
 * per-user views. Pure — no I/O — so it is trivially unit-testable (the GOV-1/HEAL-3 assembler
 * pattern). Never exposes secrets (password hash / MFA secret).
 */
@Component
public class RbacAdminAssembler {

    public RbacAdminSummary summarize(List<UserEntity> users, List<RoleEntity> roles,
                                      List<PermissionEntity> permissions) {
        if (isEmpty(users) && isEmpty(roles) && isEmpty(permissions)) {
            return RbacAdminSummary.empty();
        }

        int disabled = 0;
        int locked = 0;
        int mfa = 0;
        List<AdminUserView> views = new ArrayList<>();
        if (users != null) {
            for (UserEntity u : users) {
                if (!u.isEnabled()) disabled++;
                if (u.isAccountLocked()) locked++;
                if (u.isMfaEnabled()) mfa++;
                views.add(new AdminUserView(u.getId(), u.getUsername(), u.getEmail(), u.isEnabled(),
                        u.isMfaEnabled(), u.isAccountLocked(), u.getRoles()));
            }
        }

        List<String> roleNames = new ArrayList<>();
        if (roles != null) {
            roles.forEach(r -> roleNames.add(r.getRoleName()));
        }

        return new RbacAdminSummary(
                views.size(), disabled, locked, mfa,
                roleNames.size(), permissions != null ? permissions.size() : 0,
                views, roleNames);
    }

    private static boolean isEmpty(List<?> l) {
        return l == null || l.isEmpty();
    }
}
