package com.aiqaos.integration.plugin;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * PLG-1: plugin-runtime configuration ({@code aiqaos.plugins.*}). {@code sdkApiVersion} is the API
 * version this runtime provides (plugins are checked for compatibility against it);
 * {@code grantedPermissions} is the set of permissions the runtime is willing to grant plugins.
 */
@Component
@ConfigurationProperties(prefix = "aiqaos.plugins")
public class PluginProperties {

    private String sdkApiVersion = "1.0.0";
    private Set<String> grantedPermissions = new LinkedHashSet<>();

    public String getSdkApiVersion() { return sdkApiVersion; }
    public void setSdkApiVersion(String sdkApiVersion) { this.sdkApiVersion = sdkApiVersion; }

    public Set<String> getGrantedPermissions() { return grantedPermissions; }
    public void setGrantedPermissions(Set<String> grantedPermissions) { this.grantedPermissions = grantedPermissions; }
}
