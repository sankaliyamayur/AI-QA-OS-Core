package com.aiqaos.memory.embedding;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class EmbeddingCacheManager {

    @Cacheable(value = "embeddings", key = "#hash", unless = "#result == null")
    public float[] getCachedEmbedding(String hash) {
        return null; // Cache miss
    }

    public String computeHash(String text) {
        if (text == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 digest algorithm not found", e);
        }
    }
}
