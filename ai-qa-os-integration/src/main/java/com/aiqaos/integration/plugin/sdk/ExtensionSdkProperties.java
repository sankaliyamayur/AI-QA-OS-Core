package com.aiqaos.integration.plugin.sdk;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * PLG-3: extension-SDK configuration ({@code aiqaos.sdk.*}). {@code apiVersion} is the SDK API version
 * this runtime provides; an extension targeting a newer minor (or a different major) is refused.
 */
@Component
@ConfigurationProperties(prefix = "aiqaos.sdk")
public class ExtensionSdkProperties {

    private String apiVersion = "1.0.0";

    public String getApiVersion() { return apiVersion; }
    public void setApiVersion(String apiVersion) { this.apiVersion = apiVersion; }
}
