package com.aiqaos.security.encryption;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encryption Service — AES-256-GCM encryption for sensitive database fields.
 *
 * Protects:
 *  - API keys (OpenAI, BrowserStack, LambdaTest, Jira, Slack tokens)
 *  - User credentials and password hashes
 *  - Stored tokens and secrets
 *
 * Algorithm: AES/GCM/NoPadding (authenticated encryption, no padding oracle risk)
 * Key source: SecretManager (never hardcoded)
 */
@Component
public class EncryptionService {

    private static final String ALGORITHM  = "AES/GCM/NoPadding";
    private static final int    KEY_BITS   = 256;
    private static final int    TAG_BITS   = 128;
    private static final int    IV_BYTES   = 12;

    /**
     * Encrypt a plaintext value.
     *
     * @param plaintext  raw value to encrypt
     * @param base64Key  base64-encoded 256-bit AES key (fetched from SecretManager)
     * @return base64-encoded IV + ciphertext
     */
    public String encrypt(String plaintext, String base64Key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[IV_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to ciphertext so it can be recovered during decryption
            byte[] combined = new byte[IV_BYTES + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, IV_BYTES);
            System.arraycopy(ciphertext, 0, combined, IV_BYTES, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed", e);
        }
    }

    /**
     * Decrypt a previously encrypted value.
     *
     * @param encryptedBase64  base64-encoded IV + ciphertext (from encrypt())
     * @param base64Key        base64-encoded 256-bit AES key
     * @return original plaintext
     */
    public String decrypt(String encryptedBase64, String base64Key) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);

            byte[] iv         = new byte[IV_BYTES];
            byte[] ciphertext = new byte[combined.length - IV_BYTES];
            System.arraycopy(combined, 0,        iv,         0, IV_BYTES);
            System.arraycopy(combined, IV_BYTES, ciphertext, 0, ciphertext.length);

            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));

            byte[] plainBytes = cipher.doFinal(ciphertext);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Decryption failed", e);
        }
    }
}
