package com.aiqaos.security.auth;

import com.aiqaos.core.tenant.TenantContextHolder;
import com.aiqaos.security.config.JwtProperties;
import com.aiqaos.security.jwt.JwtTokenProvider;
import com.aiqaos.security.rbac.UserEntity;
import com.aiqaos.security.rbac.UserRepository;
import com.aiqaos.security.rbac.UserSessionEntity;
import com.aiqaos.security.rbac.UserSessionRepository;
import com.aiqaos.security.secret.SecretManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FI-ENT1-D slice 2 (ADR-058): token refresh finds the session tenant-agnostically (the session is
 * attribution-only, no @TenantId) and binds the tenant FROM the session before the @TenantId-filtered
 * user load — so refresh works with no pre-bound tenant and the user is loaded under the session's
 * tenant. Mockito-free (JDK 25).
 */
class AuthenticationServiceRefreshTenantTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("dGhpcy1pcy1hLXNlY3VyZS0yNTYtYml0LXNpZ25pbmcta2V5LWZvci10ZXN0aW5nLW1vZHVsZQ==");
        props.setExpirationMs(900000);
        props.setRefreshExpirationMs(86400000);
        props.setIssuer("test-issuer");
        props.setAllowedOrigins(new ArrayList<>());
        jwtTokenProvider = new JwtTokenProvider(props, nullSecretManagerProvider(), false);
    }

    /** No-op {@link ObjectProvider} resolving no {@link SecretManager} (avoids Mockito on JDK 25). */
    private static ObjectProvider<SecretManager> nullSecretManagerProvider() {
        return new ObjectProvider<>() {
            @Override public SecretManager getObject(Object... args) { return null; }
            @Override public SecretManager getObject() { return null; }
            @Override public SecretManager getIfAvailable() { return null; }
            @Override public SecretManager getIfUnique() { return null; }
        };
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void refreshBindsTenantFromSessionForUserLoad() {
        UUID userId = UUID.randomUUID();
        UserSessionEntity session = new UserSessionEntity();
        session.setSessionId(UUID.randomUUID());
        session.setUserId(userId);
        session.setTenantId("acme");
        session.setActive(true);
        session.setRefreshToken("rt-123");

        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername("alice");
        user.setTenantId("acme");

        AtomicReference<String> tenantAtUserLoad = new AtomicReference<>();
        UserRepository userRepo = (UserRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{UserRepository.class},
                (p, m, a) -> {
                    if ("findById".equals(m.getName())) {
                        TenantContextHolder.current().ifPresent(c -> tenantAtUserLoad.set(c.getTenantId()));
                        return Optional.of(user);
                    }
                    return defaultReturn(m);
                });
        UserSessionRepository sessionRepo = (UserSessionRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{UserSessionRepository.class},
                (p, m, a) -> {
                    if ("findByRefreshToken".equals(m.getName())) return Optional.of(session);
                    if ("save".equals(m.getName())) return a[0];
                    return defaultReturn(m);
                });

        AuthenticationService svc = new AuthenticationService(userRepo, sessionRepo, jwtTokenProvider);

        assertTrue(TenantContextHolder.current().isEmpty(), "precondition: no tenant bound");
        svc.refresh("rt-123");

        assertEquals("acme", tenantAtUserLoad.get(), "user load must run under the session's tenant");
        assertTrue(TenantContextHolder.current().isEmpty(), "context must be restored after refresh");
    }

    private static Object defaultReturn(Method m) {
        Class<?> rt = m.getReturnType();
        if (rt == Optional.class) return Optional.empty();
        if (rt == boolean.class) return false;
        return null;
    }
}
