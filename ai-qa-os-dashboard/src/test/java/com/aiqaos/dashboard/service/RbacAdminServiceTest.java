package com.aiqaos.dashboard.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.aiqaos.dashboard.dto.RbacAdminSummary;
import com.aiqaos.security.rbac.PermissionRepository;
import com.aiqaos.security.rbac.RoleRepository;
import com.aiqaos.security.rbac.UserEntity;
import com.aiqaos.security.rbac.UserRepository;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * ENT-4 (FI-ENT4-B): the service wires the RBAC repositories to the pure assembler and returns the
 * secret-free summary. Mockito-free (JDK 25) — repositories are JDK dynamic proxies.
 */
class RbacAdminServiceTest {

    @Test
    void getSummary_assemblesFromRepositoryData() {
        UserEntity enabled = new UserEntity();
        enabled.setUsername("alice");
        enabled.setEmail("alice@acme.test");
        enabled.setEnabled(true);

        UserEntity disabled = new UserEntity();
        disabled.setUsername("bob");
        disabled.setEmail("bob@acme.test");
        disabled.setEnabled(false);

        UserRepository users = repoReturning(UserRepository.class, List.of(enabled, disabled));
        RoleRepository roles = repoReturning(RoleRepository.class, List.of());
        PermissionRepository perms = repoReturning(PermissionRepository.class, List.of());

        RbacAdminService service = new RbacAdminService(users, roles, perms, new RbacAdminAssembler());
        RbacAdminSummary summary = service.getSummary();

        assertEquals(2, summary.getUserCount(), "counts both users from the repository");
        assertEquals(1, summary.getDisabledUserCount(), "one disabled user");
        assertEquals(2, summary.getUsers().size(), "a per-user view for each");
    }

    @SuppressWarnings("unchecked")
    private static <T> T repoReturning(Class<T> repoType, List<?> findAllResult) {
        return (T) Proxy.newProxyInstance(
                RbacAdminServiceTest.class.getClassLoader(), new Class<?>[]{repoType},
                (proxy, method, args) -> {
                    if ("findAll".equals(method.getName()) && (args == null || args.length == 0)) {
                        return findAllResult;
                    }
                    Class<?> rt = method.getReturnType();
                    if (rt == boolean.class) return false;
                    if (rt == long.class) return 0L;
                    if (rt == Optional.class) return Optional.empty();
                    if (rt == List.class) return List.of();
                    return null;
                });
    }
}
