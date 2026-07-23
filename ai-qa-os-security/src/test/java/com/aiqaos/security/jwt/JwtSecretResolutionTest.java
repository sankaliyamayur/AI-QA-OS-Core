package com.aiqaos.security.jwt;

import com.aiqaos.security.config.JwtProperties;
import com.aiqaos.security.rbac.UserEntity;
import com.aiqaos.security.secret.SecretManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SEC-2 — verifies JWT signing-key resolution: configured secret is used; a missing secret fails
 * fast when enforcement is on, and yields a usable ephemeral key when enforcement is off.
 */
class JwtSecretResolutionTest {

    private static final String TEST_SECRET =
            "test-only-non-production-hmac-signing-key-do-not-use-in-any-real-environment";

    /** A no-op {@link ObjectProvider} that resolves no {@link SecretManager} (avoids Mockito on JDK 25). */
    private ObjectProvider<SecretManager> noSecretManager() {
        return new ObjectProvider<>() {
            @Override public SecretManager getObject(Object... args) { return null; }
            @Override public SecretManager getObject() { return null; }
            @Override public SecretManager getIfAvailable() { return null; }
            @Override public SecretManager getIfUnique() { return null; }
        };
    }

    private JwtProperties props(String secret) {
        JwtProperties p = new JwtProperties();
        p.setSecret(secret);
        p.setExpirationMs(900000);
        p.setRefreshExpirationMs(86400000);
        p.setIssuer("test-issuer");
        return p;
    }

    @Test
    void usesConfiguredSecret() {
        JwtTokenProvider provider = new JwtTokenProvider(props(TEST_SECRET), noSecretManager(), false);
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("qa");
        String token = provider.generateAccessToken(user, UUID.randomUUID(), 1);
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
    }

    @Test
    void failsFastWhenEnforcedAndSecretMissing() {
        assertThrows(IllegalStateException.class,
                () -> new JwtTokenProvider(props(null), noSecretManager(), true));
    }

    @Test
    void usesEphemeralKeyWhenNotEnforcedAndSecretMissing() {
        JwtTokenProvider provider = new JwtTokenProvider(props(null), noSecretManager(), false);
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("qa");
        String token = provider.generateAccessToken(user, UUID.randomUUID(), 1);
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
    }
}
