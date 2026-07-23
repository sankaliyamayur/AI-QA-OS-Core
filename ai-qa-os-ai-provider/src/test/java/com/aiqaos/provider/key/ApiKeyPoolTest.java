package com.aiqaos.provider.key;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.security.secret.SecretManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for API key rotation / cooldown / masking (MNT-3). */
class ApiKeyPoolTest {

    private static final String BASE = "GEMINI_API_KEY";

    /** Stub SecretManager backed by a fixed map. */
    private static SecretManager secrets(Map<String, String> values) {
        return name -> values.get(name);
    }

    @Test
    void parsesCsvSingleAndNumberedKeysInOrderWithoutDuplicates() {
        Map<String, String> v = new HashMap<>();
        v.put(BASE + "S", "k1, k2 , k1");   // CSV with a duplicate + whitespace
        v.put(BASE, "k3");                   // single
        v.put(BASE + "_2", "k2");            // numbered duplicate of CSV
        v.put(BASE + "_3", "k4");            // numbered new

        ApiKeyPool pool = new ApiKeyPool(secrets(v), BASE, Duration.ofHours(1));

        assertThat(pool.allKeys()).containsExactly("k1", "k2", "k3", "k4");
        assertThat(pool.hasKeys()).isTrue();
    }

    @Test
    void hasKeysIsFalseWhenNothingConfigured() {
        ApiKeyPool pool = new ApiKeyPool(secrets(Map.of()), BASE, Duration.ofHours(1));
        assertThat(pool.hasKeys()).isFalse();
        assertThat(pool.availableKeys()).isEmpty();
    }

    @Test
    void exhaustedKeyIsSkippedDuringCooldown() {
        ApiKeyPool pool = new ApiKeyPool(secrets(Map.of(BASE + "S", "k1,k2,k3")), BASE, Duration.ofHours(1));

        pool.markExhausted("k2");

        assertThat(pool.availableKeys()).containsExactly("k1", "k3");
    }

    @Test
    void fallsBackToAllKeysWhenEveryKeyIsInCooldown() {
        ApiKeyPool pool = new ApiKeyPool(secrets(Map.of(BASE + "S", "k1,k2")), BASE, Duration.ofHours(1));

        pool.markExhausted("k1");
        pool.markExhausted("k2");

        // A stale cooldown must not turn a transient error into a hard outage.
        assertThat(pool.availableKeys()).containsExactly("k1", "k2");
    }

    @Test
    void maskKeyRevealsOnlyLastFour() {
        assertThat(ApiKeyPool.maskKey("sk-abcdef1234")).isEqualTo("...1234");
        assertThat(ApiKeyPool.maskKey("abcd")).isEqualTo("****");
        assertThat(ApiKeyPool.maskKey(null)).isEqualTo("****");
    }
}
