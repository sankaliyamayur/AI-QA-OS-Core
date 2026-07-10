package com.aiqaos.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiqaos.security")
public class SecurityProperties {
    private boolean enabled = false;
    private String jwtSecret = "default-enterprise-secret-key-default-enterprise-secret-key";
    private long tokenValiditySeconds = 86400;
    private String defaultAuditor = "SYSTEM";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getJwtSecret() { return jwtSecret; }
    public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
    public long getTokenValiditySeconds() { return tokenValiditySeconds; }
    public void setTokenValiditySeconds(long tokenValiditySeconds) { this.tokenValiditySeconds = tokenValiditySeconds; }
    public String getDefaultAuditor() { return defaultAuditor; }
    public void setDefaultAuditor(String defaultAuditor) { this.defaultAuditor = defaultAuditor; }
}