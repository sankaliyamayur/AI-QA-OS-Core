package com.aiqaos.execution.artifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** SEC-6 (ADR-076): HMAC-SHA256 artifact signing — round-trip, tamper detection, key isolation, fail-closed. */
class ArtifactSignerTest {

    private static ArtifactSigner signer(boolean enabled, String secret) {
        ArtifactSignatureProperties p = new ArtifactSignatureProperties();
        p.setEnabled(enabled);
        p.setSecret(secret);
        return new ArtifactSigner(p);
    }

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void signThenVerify_roundTrips() {
        ArtifactSigner s = signer(true, "top-secret-key");
        String sig = s.sign(bytes("screenshot-bytes"));
        assertTrue(s.verify(bytes("screenshot-bytes"), sig));
    }

    @Test
    void tamperedContent_failsVerification() {
        ArtifactSigner s = signer(true, "top-secret-key");
        String sig = s.sign(bytes("original"));
        assertFalse(s.verify(bytes("tampered"), sig));
    }

    @Test
    void differentSecret_failsVerification() {
        String sig = signer(true, "key-A").sign(bytes("data"));
        assertFalse(signer(true, "key-B").verify(bytes("data"), sig));
    }

    @Test
    void signatureIsDeterministic() {
        assertEquals(signer(true, "k").sign(bytes("x")), signer(true, "k").sign(bytes("x")));
        assertNotEquals(signer(true, "k").sign(bytes("x")), signer(true, "k").sign(bytes("y")));
    }

    @Test
    void enabledButBlankSecret_failsClosed() {
        ArtifactSigner s = signer(true, "   ");
        assertFalse(s.isSigningEnabled(), "blank secret -> signing not active (fail closed)");
        assertFalse(s.verify(bytes("x"), "anything"));
        assertThrows(IllegalStateException.class, () -> s.sign(bytes("x")));
    }

    @Test
    void disabled_isNotSigning() {
        assertFalse(signer(false, "k").isSigningEnabled());
    }

    @Test
    void verify_nullSignature_isFalse() {
        assertFalse(signer(true, "k").verify(bytes("x"), null));
    }
}
