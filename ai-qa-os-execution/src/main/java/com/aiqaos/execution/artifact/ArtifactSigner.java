package com.aiqaos.execution.artifact;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SEC-6 (ADR-076): signs an artifact's bytes with HMAC-SHA256 and verifies them, so tampering in the
 * object store is detectable (integrity / provenance). Opt-in ({@code aiqaos.artifacts.signing.enabled});
 * the key is env/secret-injected (SEC-2). Verification uses a constant-time comparison.
 */
@Component
@EnableConfigurationProperties(ArtifactSignatureProperties.class)
public class ArtifactSigner {

    private static final String ALGORITHM = "HmacSHA256";

    private final boolean enabled;
    private final String secret;

    public ArtifactSigner(ArtifactSignatureProperties properties) {
        this.enabled = properties.isEnabled();
        this.secret = properties.getSecret() == null ? "" : properties.getSecret();
    }

    /** Signing is active only when enabled AND a non-blank secret is configured (else fail closed). */
    public boolean isSigningEnabled() {
        return enabled && !secret.isBlank();
    }

    /** HMAC-SHA256 of {@code content}, hex-encoded. Requires a configured secret. */
    public String sign(byte[] content) {
        if (secret.isBlank()) {
            throw new IllegalStateException("artifact signing secret is not configured");
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return toHex(mac.doFinal(content));
        } catch (Exception e) {
            throw new IllegalStateException("artifact signing failed", e);
        }
    }

    /** True iff {@code signatureHex} is the HMAC-SHA256 of {@code content} (constant-time compare). */
    public boolean verify(byte[] content, String signatureHex) {
        if (signatureHex == null || secret.isBlank()) {
            return false;
        }
        byte[] expected = sign(content).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, signatureHex.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
