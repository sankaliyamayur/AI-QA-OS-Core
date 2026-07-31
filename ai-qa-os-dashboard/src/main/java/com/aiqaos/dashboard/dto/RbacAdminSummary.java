package com.aiqaos.dashboard.dto;

import java.util.Collections;
import java.util.List;

/**
 * ENT-4: the RBAC admin read-model — headline counts, security-posture tallies (disabled / locked /
 * MFA-enabled users), and the per-user views + role names an administrator sees. Composed from the
 * existing {@code ai-qa-os-security} entities (no new persistence).
 */
public final class RbacAdminSummary {

    private final int userCount;
    private final int disabledUserCount;
    private final int lockedUserCount;
    private final int mfaEnabledCount;
    private final int roleCount;
    private final int permissionCount;
    private final List<AdminUserView> users;
    private final List<String> roleNames;

    public RbacAdminSummary(int userCount, int disabledUserCount, int lockedUserCount,
                            int mfaEnabledCount, int roleCount, int permissionCount,
                            List<AdminUserView> users, List<String> roleNames) {
        this.userCount = userCount;
        this.disabledUserCount = disabledUserCount;
        this.lockedUserCount = lockedUserCount;
        this.mfaEnabledCount = mfaEnabledCount;
        this.roleCount = roleCount;
        this.permissionCount = permissionCount;
        this.users = immutable(users);
        this.roleNames = immutable(roleNames);
    }

    private static <T> List<T> immutable(List<T> l) {
        return l != null ? Collections.unmodifiableList(List.copyOf(l)) : List.of();
    }

    public static RbacAdminSummary empty() {
        return new RbacAdminSummary(0, 0, 0, 0, 0, 0, List.of(), List.of());
    }

    public int getUserCount() { return userCount; }
    public int getDisabledUserCount() { return disabledUserCount; }
    public int getLockedUserCount() { return lockedUserCount; }
    public int getMfaEnabledCount() { return mfaEnabledCount; }
    public int getRoleCount() { return roleCount; }
    public int getPermissionCount() { return permissionCount; }
    public List<AdminUserView> getUsers() { return users; }
    public List<String> getRoleNames() { return roleNames; }
}
