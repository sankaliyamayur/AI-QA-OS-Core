package com.aiqaos.execution.artifact;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SEC-6 (ADR-076): configuration for artifact content signing (integrity / provenance).
 *
 * <p>Disabled by default (non-breaking). When enabled, each stored artifact gets an HMAC-SHA256
 * signature (a {@code <key>.sig} sidecar) that is re-verified on retrieval — tamper-evidence for a
 * regulated-deployment posture. SEC-2: the {@code secret} is injected from env/secret store, never
 * committed; enabled with a blank secret fails closed (treated as unsigned).
 */
@ConfigurationProperties(prefix = "aiqaos.artifacts.signing")
public class ArtifactSignatureProperties {

    private boolean enabled = false;
    private String secret = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
