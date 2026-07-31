package com.aiqaos.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.dashboard.dto.AdminUserView;
import com.aiqaos.dashboard.dto.RbacAdminSummary;
import com.aiqaos.security.rbac.PermissionEntity;
import com.aiqaos.security.rbac.RoleEntity;
import com.aiqaos.security.rbac.UserEntity;
import java.util.List;
import org.junit.jupiter.api.Test;

/** ENT-4: unit tests for composing RBAC entities into the admin read-model. No Mockito. */
class RbacAdminAssemblerTest {

    private final RbacAdminAssembler assembler = new RbacAdminAssembler();

    private UserEntity user(String name, String email, boolean enabled, boolean mfa, boolean locked) {
        UserEntity u = new UserEntity();
        u.setUsername(name);
        u.setEmail(email);
        u.setEnabled(enabled);
        u.setMfaEnabled(mfa);
        u.setAccountLocked(locked);
        return u;
    }

    private RoleEntity role(String name) {
        RoleEntity r = new RoleEntity();
        r.setRoleName(name);
        return r;
    }

    @Test
    void countsUsersRolesPermissionsAndSecurityPosture() {
        RbacAdminSummary s = assembler.summarize(
                List.of(
                        user("admin", "a@x", true, true, false),
                        user("bob", "b@x", false, false, false),   // disabled
                        user("eve", "e@x", true, true, true)),      // locked, mfa
                List.of(role("ADMIN"), role("QA")),
                List.of(new PermissionEntity(), new PermissionEntity(),
                        new PermissionEntity(), new PermissionEntity()));

        assertThat(s.getUserCount()).isEqualTo(3);
        assertThat(s.getDisabledUserCount()).isEqualTo(1);
        assertThat(s.getLockedUserCount()).isEqualTo(1);
        assertThat(s.getMfaEnabledCount()).isEqualTo(2);
        assertThat(s.getRoleCount()).isEqualTo(2);
        assertThat(s.getPermissionCount()).isEqualTo(4);
        assertThat(s.getRoleNames()).containsExactlyInAnyOrder("ADMIN", "QA");
    }

    @Test
    void userViewMapsIdentityAndPostureWithoutSecrets() {
        RbacAdminSummary s = assembler.summarize(
                List.of(user("admin", "a@x", true, true, false)), List.of(), List.of());
        AdminUserView v = s.getUsers().get(0);
        assertThat(v.getUsername()).isEqualTo("admin");
        assertThat(v.getEmail()).isEqualTo("a@x");
        assertThat(v.isEnabled()).isTrue();
        assertThat(v.isMfaEnabled()).isTrue();
        assertThat(v.isAccountLocked()).isFalse();
    }

    @Test
    void emptyInputsYieldEmptySummary() {
        assertThat(assembler.summarize(List.of(), List.of(), List.of()).getUserCount()).isZero();
        assertThat(assembler.summarize(null, null, null).getRoleCount()).isZero();
    }
}
