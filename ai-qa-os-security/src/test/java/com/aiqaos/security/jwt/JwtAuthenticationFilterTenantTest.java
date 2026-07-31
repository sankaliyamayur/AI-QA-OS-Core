package com.aiqaos.security.jwt;

import com.aiqaos.core.tenant.TenantContextHolder;
import com.aiqaos.security.config.JwtProperties;
import com.aiqaos.security.rbac.UserEntity;
import com.aiqaos.security.rbac.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FI-ENT1-D (ADR-055): the signed JWT — not the (spoofable) X-Tenant-ID header — is the authoritative
 * tenant for an authenticated request. This proves {@link JwtAuthenticationFilter} binds the token's
 * tenant BEFORE loading the user (so the load is {@code @TenantId}-filtered) and for the downstream
 * chain, then restores the previous context afterwards. Mockito-free (JDK 25).
 */
class JwtAuthenticationFilterTenantTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("dGhpcy1pcy1hLXNlY3VyZS0yNTYtYml0LXNpZ25pbmcta2V5LWZvci10ZXN0aW5nLW1vZHVsZQ==");
        props.setExpirationMs(900000);
        props.setRefreshExpirationMs(86400000);
        props.setIssuer("test-issuer");
        props.setAllowedOrigins(new ArrayList<>());
        jwtTokenProvider = new JwtTokenProvider(props, JwtTokenProviderTest.nullSecretManagerProvider(), false);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void bindsTokenTenantDuringUserLoadAndDownstream_thenRestores() throws Exception {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername("alice");
        user.setTenantId("acme");
        user.setEnabled(true);
        String token = jwtTokenProvider.generateAccessToken(user, UUID.randomUUID(), 1);

        AtomicReference<String> tenantAtLoad = new AtomicReference<>();
        UserRepository repo = tenantCapturingRepo(userId, user, tenantAtLoad);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, singletonProvider(repo));

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/anything");
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();
        AtomicReference<String> tenantInChain = new AtomicReference<>();
        MockFilterChain chain = new MockFilterChain() {
            @Override
            public void doFilter(ServletRequest rq, ServletResponse rs) {
                TenantContextHolder.current().ifPresent(c -> tenantInChain.set(c.getTenantId()));
            }
        };

        assertTrue(TenantContextHolder.current().isEmpty(), "precondition: no tenant bound");
        filter.doFilter(req, res, chain);

        assertEquals("acme", tenantAtLoad.get(), "user load must run under the token's tenant");
        assertEquals("acme", tenantInChain.get(), "downstream chain must run under the token's tenant");
        assertTrue(TenantContextHolder.current().isEmpty(), "context must be restored (cleared) after the request");
    }

    /** JDK dynamic-proxy {@link UserRepository} whose findById records the bound tenant at call time. */
    private UserRepository tenantCapturingRepo(UUID id, UserEntity user, AtomicReference<String> capture) {
        return (UserRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName()) && args != null && args.length == 1 && id.equals(args[0])) {
                        TenantContextHolder.current().ifPresent(c -> capture.set(c.getTenantId()));
                        return Optional.of(user);
                    }
                    Class<?> rt = method.getReturnType();
                    if (rt == Optional.class) return Optional.empty();
                    if (rt == boolean.class) return false;
                    return null;
                });
    }

    private ObjectProvider<UserRepository> singletonProvider(UserRepository repo) {
        return new ObjectProvider<>() {
            @Override public UserRepository getObject(Object... args) { return repo; }
            @Override public UserRepository getObject() { return repo; }
            @Override public UserRepository getIfAvailable() { return repo; }
            @Override public UserRepository getIfUnique() { return repo; }
        };
    }
}
