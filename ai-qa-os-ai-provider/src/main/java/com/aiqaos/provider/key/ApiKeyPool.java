package com.aiqaos.provider.key;

import com.aiqaos.security.secret.SecretManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An ordered set of interchangeable API keys for one provider, so a single key
 * hitting its quota does not stop execution.
 *
 * <p>Keys are read from the secret store under, for a base name of {@code GEMINI_API_KEY}:
 * <ul>
 *   <li>{@code GEMINI_API_KEYS} — comma-separated list</li>
 *   <li>{@code GEMINI_API_KEY} — the single-key form</li>
 *   <li>{@code GEMINI_API_KEY_2} … {@code GEMINI_API_KEY_10} — numbered additions</li>
 * </ul>
 * Duplicates are collapsed and order is preserved, so the existing single-key
 * setup keeps working unchanged and extra keys are purely additive.
 *
 * <p>A key reported as exhausted is skipped until its cooldown expires rather than
 * being discarded, because provider quotas replenish.
 */
public class ApiKeyPool {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyPool.class);

    private static final int MAX_NUMBERED_KEYS = 10;

    private final SecretManager secretManager;
    private final String baseName;
    private final Duration cooldown;

    /** Key -> instant at which it becomes usable again. */
    private final Map<String, Instant> exhaustedUntil = new ConcurrentHashMap<>();

    public ApiKeyPool(SecretManager secretManager, String baseName, Duration cooldown) {
        this.secretManager = secretManager;
        this.baseName = baseName;
        this.cooldown = cooldown;
    }

    /**
     * Every configured key, in preference order, including exhausted ones.
     * Read fresh each call so a key added to the environment does not require a restart.
     */
    public List<String> allKeys() {
        Set<String> keys = new LinkedHashSet<>();

        String csv = secretManager.getSecret(baseName + "S");
        if (csv != null && !csv.isBlank()) {
            for (String part : csv.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    keys.add(trimmed);
                }
            }
        }

        String single = secretManager.getSecret(baseName);
        if (single != null && !single.isBlank()) {
            keys.add(single.trim());
        }

        for (int i = 2; i <= MAX_NUMBERED_KEYS; i++) {
            String numbered = secretManager.getSecret(baseName + "_" + i);
            if (numbered != null && !numbered.isBlank()) {
                keys.add(numbered.trim());
            }
        }

        return new ArrayList<>(keys);
    }

    /**
     * Keys currently worth trying. Falls back to the full set when every key is in
     * cooldown — a stale cooldown should not turn a transient quota error into a
     * hard outage.
     */
    public List<String> availableKeys() {
        List<String> all = allKeys();
        Instant now = Instant.now();

        List<String> usable = new ArrayList<>();
        for (String key : all) {
            Instant until = exhaustedUntil.get(key);
            if (until == null || now.isAfter(until)) {
                usable.add(key);
            }
        }

        if (usable.isEmpty() && !all.isEmpty()) {
            log.warn("All {} key(s) for {} are in cooldown; retrying them anyway", all.size(), baseName);
            return all;
        }
        return usable;
    }

    /** Marks a key as spent so subsequent requests skip it until its cooldown lapses. */
    public void markExhausted(String key) {
        exhaustedUntil.put(key, Instant.now().plus(cooldown));
        log.warn("{} key ending '{}' is exhausted; skipping it for {}",
                baseName, maskKey(key), cooldown);
    }

    public boolean hasKeys() {
        return !allKeys().isEmpty();
    }

    /** Last four characters only — enough to tell keys apart in logs without leaking them. */
    public static String maskKey(String key) {
        if (key == null || key.length() <= 4) {
            return "****";
        }
        return "..." + key.substring(key.length() - 4);
    }
}
