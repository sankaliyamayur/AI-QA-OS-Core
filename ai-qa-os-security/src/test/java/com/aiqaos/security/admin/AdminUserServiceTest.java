package com.aiqaos.security.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.security.admin.AdminUserRequests.CreateUserRequest;
import com.aiqaos.security.rbac.RoleEntity;
import com.aiqaos.security.rbac.RoleRepository;
import com.aiqaos.security.rbac.UserEntity;
import com.aiqaos.security.rbac.UserRepository;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

/**
 * FI-ENT4-A (ADR-067): the admin write service. Mockito-free (JDK 25) — the JPA repositories are
 * hand-backed JDK dynamic proxies over an in-memory store. Controller authz ({@code hasRole('ADMIN')})
 * is Spring config and is not exercised here; the business guards (validation, duplicates, unknown
 * roles, self-lockout, self-demotion) are.
 */
class AdminUserServiceTest {

    private final Map<UUID, UserEntity> userStore = new HashMap<>();
    private final List<RoleEntity> roleCatalog = new ArrayList<>();
    private AdminUserService service;

    @BeforeEach
    void setUp() {
        userStore.clear();
        roleCatalog.clear();
        roleCatalog.add(role("ADMIN"));
        roleCatalog.add(role("QA_MANAGER"));
        service = new AdminUserService(userRepositoryProxy(), roleRepositoryProxy());
    }

    // --- create ---------------------------------------------------------------------------------

    @Test
    void create_hashesPassword_validatesRoles_enabled() {
        AdminUserView view = service.create(new CreateUserRequest("alice", "alice@acme.io", "s3cret", List.of("admin")));

        assertTrue(view.enabled());
        assertEquals(List.of("ADMIN"), view.roles(), "role canonicalized from catalog");
        UserEntity saved = userStore.get(view.id());
        assertFalse("s3cret".equals(saved.getPasswordHash()), "password must be hashed, not stored plaintext");
        assertTrue(new BCryptPasswordEncoder().matches("s3cret", saved.getPasswordHash()), "hash verifies");
    }

    @Test
    void create_blankUsername_is400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(new CreateUserRequest("  ", "a@b.io", "pw", List.of())));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void create_duplicateUsername_is409() {
        service.create(new CreateUserRequest("alice", "alice@acme.io", "pw", List.of()));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(new CreateUserRequest("alice", "other@acme.io", "pw", List.of())));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void create_unknownRole_is400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.create(new CreateUserRequest("bob", "bob@acme.io", "pw", List.of("WIZARD"))));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // --- enable/disable -------------------------------------------------------------------------

    @Test
    void setEnabled_disableOther_flips() {
        UUID id = seedUser("carol", List.of());
        AdminUserView view = service.setEnabled(id, false, UUID.randomUUID());
        assertFalse(view.enabled());
    }

    @Test
    void setEnabled_disableSelf_is400() {
        UUID id = seedUser("selfadmin", List.of("ADMIN"));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.setEnabled(id, false, id));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // --- assign roles ---------------------------------------------------------------------------

    @Test
    void setRoles_replacesAndCanonicalizes() {
        UUID id = seedUser("dave", List.of("ADMIN"));
        AdminUserView view = service.setRoles(id, List.of("qa_manager"), UUID.randomUUID());
        assertEquals(List.of("QA_MANAGER"), view.roles());
    }

    @Test
    void setRoles_removingOwnAdmin_is400() {
        UUID id = seedUser("selfadmin", List.of("ADMIN"));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.setRoles(id, List.of("QA_MANAGER"), id));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    // --- fixtures / proxies ---------------------------------------------------------------------

    private UUID seedUser(String username, List<String> roles) {
        UserEntity u = new UserEntity();
        u.setId(UUID.randomUUID());
        u.setUsername(username);
        u.setEmail(username + "@acme.io");
        u.setPasswordHash("x");
        u.setEnabled(true);
        u.setRoles(new ArrayList<>(roles));
        userStore.put(u.getId(), u);
        return u.getId();
    }

    private static RoleEntity role(String name) {
        RoleEntity r = new RoleEntity();
        r.setRoleName(name);
        return r;
    }

    private UserRepository userRepositoryProxy() {
        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "findByUsername":
                    return userStore.values().stream().filter(u -> args[0].equals(u.getUsername())).findFirst();
                case "findByEmail":
                    return userStore.values().stream().filter(u -> args[0].equals(u.getEmail())).findFirst();
                case "findById":
                    return Optional.ofNullable(userStore.get(args[0]));
                case "save": {
                    UserEntity u = (UserEntity) args[0];
                    if (u.getId() == null) {
                        u.setId(UUID.randomUUID());
                    }
                    userStore.put(u.getId(), u);
                    return u;
                }
                default:
                    return defaultReturn(method);
            }
        };
        return (UserRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{UserRepository.class}, handler);
    }

    private RoleRepository roleRepositoryProxy() {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("findAll".equals(method.getName()) && (args == null || args.length == 0)) {
                return new ArrayList<>(roleCatalog);
            }
            return defaultReturn(method);
        };
        return (RoleRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{RoleRepository.class}, handler);
    }

    private static Object defaultReturn(Method method) {
        Class<?> rt = method.getReturnType();
        if (rt.equals(Optional.class)) {
            return Optional.empty();
        }
        if (rt.equals(boolean.class)) {
            return false;
        }
        if (rt.equals(long.class)) {
            return 0L;
        }
        if (rt.equals(List.class)) {
            return new ArrayList<>();
        }
        return null;
    }
}
