package com.aiqaos.security.jwt;

import com.aiqaos.security.config.JwtProperties;
import com.aiqaos.security.rbac.UserEntity;
import com.aiqaos.security.secret.SecretManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    /**
     * SEC-2 — the signing key is resolved from configuration/secret store, never a committed literal.
     * Resolution order: {@code security.jwt.secret} (env-injectable) then {@link SecretManager}
     * ({@code JWT_SECRET}). If unresolved: fail fast when security enforcement is on
     * ({@code aiqaos.security.enabled=true}); otherwise use a non-persistent ephemeral key with a
     * loud warning so local/test/CI contexts still start.
     */
    public JwtTokenProvider(JwtProperties jwtProperties,
                            ObjectProvider<SecretManager> secretManagerProvider,
                            @Value("${aiqaos.security.enabled:false}") boolean securityEnabled) {
        this.jwtProperties = jwtProperties;
        this.key = resolveSigningKey(jwtProperties, secretManagerProvider, securityEnabled);
    }

    private static SecretKey resolveSigningKey(JwtProperties jwtProperties,
                                               ObjectProvider<SecretManager> secretManagerProvider,
                                               boolean securityEnabled) {
        String secret = (jwtProperties != null) ? jwtProperties.getSecret() : null;

        if (isBlank(secret) && secretManagerProvider != null) {
            SecretManager secretManager = secretManagerProvider.getIfAvailable();
            if (secretManager != null) {
                secret = secretManager.getSecret("JWT_SECRET");
            }
        }

        if (isBlank(secret)) {
            if (securityEnabled) {
                throw new IllegalStateException(
                        "JWT signing secret is not configured. Set JWT_SECRET (or security.jwt.secret) — "
                        + "required when aiqaos.security.enabled=true.");
            }
            log.warn("SEC-2: no JWT signing secret configured and security enforcement is off; using a "
                    + "non-persistent ephemeral key. Tokens will not survive a restart. Set JWT_SECRET "
                    + "for any real environment.");
            return Jwts.SIG.HS256.key().build();
        }

        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public String generateAccessToken(UserEntity user, UUID sessionId, int tokenVersion) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("tenantId", user.getTenantId()) // FI-ENT1-D: String tenant id (matches TenantContext)
                .claim("organizationId", user.getOrganizationId() != null ? user.getOrganizationId().toString() : null)
                .claim("workspaceId", user.getWorkspaceId() != null ? user.getWorkspaceId().toString() : null)
                .claim("sessionId", sessionId.toString())
                .claim("tokenVersion", tokenVersion)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(UserEntity user, UUID sessionId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshExpirationMs());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("sessionId", sessionId.toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
