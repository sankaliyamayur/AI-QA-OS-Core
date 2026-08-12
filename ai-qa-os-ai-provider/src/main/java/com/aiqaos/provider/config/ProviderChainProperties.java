package com.aiqaos.provider.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Failover chain configuration: which real providers are tried, in what order, and whether the
 * Simulator is permitted at all.
 *
 * <pre>
 * aiqaos:
 *   provider:
 *     mode: real                          # real | simulated
 *     chain: openai,claude,gemini,ollama
 * </pre>
 *
 * <p><b>Registered with @Component deliberately.</b> @ConfigurationProperties alone binds nothing
 * unless something declares it — this class was originally annotation-only and no
 * @EnableConfigurationProperties listed it, so aiqaos.provider.mode and .chain silently did not
 * bind and LLMProviderManager fell through to hardcoded defaults. The defaults happened to be the
 * safe ones, which is exactly why it went unnoticed.
 *
 * <p><b>Mode is the safety switch.</b> In {@code real} — the default — the Simulator is excluded
 * from the chain entirely, so no provider failure can reach it. It becomes reachable only when mode
 * is {@code simulated} or the request explicitly names a simulator/mock model. Defaulting to real
 * is deliberate: a misconfiguration should produce a loud failure, not a quiet fake pass.
 */
@Component
@ConfigurationProperties(prefix = "aiqaos.provider")
public class ProviderChainProperties {

    public enum Mode {
        /** Only real providers. A Simulator response is a bug and is rejected. */
        REAL,
        /** Simulator permitted — for local development and tests. */
        SIMULATED
    }

    private String mode = "real";

    /** Priority order. Names match LLMProvider.getProviderName(), case-insensitively. */
    private String chain = "openai,claude,gemini,ollama";

    /** Single immediate retry on a 429/503 before moving to the next provider. */
    private boolean retryOnRateLimit = true;

    /** Backoff before that retry. */
    private long retryBackoffMillis = 1000L;

    public Mode resolvedMode() {
        return "simulated".equalsIgnoreCase(mode == null ? "" : mode.trim()) ? Mode.SIMULATED : Mode.REAL;
    }

    public List<String> chainOrder() {
        if (chain == null || chain.isBlank()) {
            return List.of("openai", "claude", "gemini", "ollama");
        }
        return Arrays.stream(chain.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList();
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public boolean isRetryOnRateLimit() {
        return retryOnRateLimit;
    }

    public void setRetryOnRateLimit(boolean retryOnRateLimit) {
        this.retryOnRateLimit = retryOnRateLimit;
    }

    public long getRetryBackoffMillis() {
        return retryBackoffMillis;
    }

    public void setRetryBackoffMillis(long retryBackoffMillis) {
        this.retryBackoffMillis = retryBackoffMillis;
    }
}
