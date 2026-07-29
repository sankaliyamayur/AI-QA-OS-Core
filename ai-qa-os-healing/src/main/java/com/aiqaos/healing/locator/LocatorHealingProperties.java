package com.aiqaos.healing.locator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HEAL-1: locator-healing configuration ({@code aiqaos.healing.locator.*}). {@code minConfidence} is
 * the local threshold used to decide auto-apply <em>when the AI-1 confidence gate is not present</em>
 * (e.g. in a healing-only deployment); when the gate is wired, it decides instead.
 */
@Component
@ConfigurationProperties(prefix = "aiqaos.healing.locator")
public class LocatorHealingProperties {

    private double minConfidence = 0.70;

    public double getMinConfidence() { return minConfidence; }
    public void setMinConfidence(double minConfidence) { this.minConfidence = minConfidence; }
}
