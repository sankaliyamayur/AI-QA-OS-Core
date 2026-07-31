package com.aiqaos.security.jwt;

import com.aiqaos.security.config.JwtProperties;
import com.aiqaos.security.rbac.UserEntity;
import com.aiqaos.security.secret.SecretManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    public void setUp() {
        jwtProperties = new JwtProperties();
        // Setup secure hex key properties mock for testing signature validation
        jwtProperties.setSecret("dGhpcy1pcy1hLXNlY3VyZS0yNTYtYml0LXNpZ25pbmcta2V5LWZvci10ZXN0aW5nLW1vZHVsZQ==");
        jwtProperties.setExpirationMs(900000); // 15 mins
        jwtProperties.setRefreshExpirationMs(86400000); // 24 hours
        jwtProperties.setIssuer("test-issuer");
        jwtProperties.setAllowedOrigins(new ArrayList<>());

        jwtTokenProvider = new JwtTokenProvider(jwtProperties, nullSecretManagerProvider(), false);
    }

    /** A no-op {@link ObjectProvider} that resolves no {@link SecretManager} (avoids Mockito on JDK 25). */
    static ObjectProvider<SecretManager> nullSecretManagerProvider() {
        return new ObjectProvider<>() {
            @Override public SecretManager getObject(Object... args) { return null; }
            @Override public SecretManager getObject() { return null; }
            @Override public SecretManager getIfAvailable() { return null; }
            @Override public SecretManager getIfUnique() { return null; }
        };
    }

    @Test
    public void testGenerateAndValidateToken() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("qa_engineer");
        user.setTenantId("acme"); // FI-ENT1-D: tenant id is now a String discriminator

        UUID sessionId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(user, sessionId, 1);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(user.getId().toString(), jwtTokenProvider.getClaimsFromToken(token).getSubject());
    }

    @Test
    public void testInvalidTokenFails() {
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token.signature"));
    }
}
