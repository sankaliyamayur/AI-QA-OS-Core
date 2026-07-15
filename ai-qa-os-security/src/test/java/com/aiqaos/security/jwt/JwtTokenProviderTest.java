package com.aiqaos.security.jwt;

import com.aiqaos.security.config.JwtProperties;
import com.aiqaos.security.rbac.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Test
    public void testGenerateAndValidateToken() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("qa_engineer");
        user.setTenantId(UUID.randomUUID());

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
