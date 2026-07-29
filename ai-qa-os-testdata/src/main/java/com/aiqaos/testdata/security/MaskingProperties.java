package com.aiqaos.testdata.security;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MOD-4: masking configuration ({@code aiqaos.testdata.masking.*}). The default strategy is
 * {@code PARTIAL}; it can be overridden globally or per {@link PiiType} (keyed by the enum name,
 * e.g. {@code aiqaos.testdata.masking.strategies.CREDIT_CARD=HASH}). {@code hashSalt} seeds the
 * deterministic {@code HASH} strategy.
 */
@Component
@ConfigurationProperties(prefix = "aiqaos.testdata.masking")
public class MaskingProperties {

    private String defaultStrategy = "PARTIAL";
    private Map<String, String> strategies = new HashMap<>();
    private String hashSalt = "aiqaos";
    private String maskChar = "*";

    /** Resolve the strategy for a type: per-type override, else the default; invalid → PARTIAL. */
    public MaskingStrategy strategyFor(PiiType type) {
        String name = strategies.getOrDefault(type.name(), defaultStrategy);
        try {
            return MaskingStrategy.valueOf(name.trim().toUpperCase());
        } catch (RuntimeException ex) {
            return MaskingStrategy.PARTIAL;
        }
    }

    public String getDefaultStrategy() { return defaultStrategy; }
    public void setDefaultStrategy(String defaultStrategy) { this.defaultStrategy = defaultStrategy; }

    public Map<String, String> getStrategies() { return strategies; }
    public void setStrategies(Map<String, String> strategies) { this.strategies = strategies; }

    public String getHashSalt() { return hashSalt; }
    public void setHashSalt(String hashSalt) { this.hashSalt = hashSalt; }

    public String getMaskChar() { return maskChar; }
    public void setMaskChar(String maskChar) { this.maskChar = maskChar; }
}
