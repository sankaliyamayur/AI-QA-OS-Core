package com.aiqaos.healing.locator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HEAL-1: the input to locator healing — the locator that broke, plus whatever is known about the
 * target element (its attributes, an optional description) to reason a better alternative from.
 */
public final class LocatorHealingRequest {

    private final String brokenLocator;
    private final LocatorStrategy brokenStrategy; // nullable
    private final String elementDescription;      // nullable
    private final Map<String, String> attributes; // never null
    private final String correlationId;           // nullable

    public LocatorHealingRequest(String brokenLocator, LocatorStrategy brokenStrategy,
                                 String elementDescription, Map<String, String> attributes,
                                 String correlationId) {
        this.brokenLocator = brokenLocator;
        this.brokenStrategy = brokenStrategy;
        this.elementDescription = elementDescription;
        this.attributes = attributes != null ? new LinkedHashMap<>(attributes) : new LinkedHashMap<>();
        this.correlationId = correlationId;
    }

    /** Convenience for the common case: a broken locator plus known element attributes. */
    public static LocatorHealingRequest of(String brokenLocator, Map<String, String> attributes) {
        return new LocatorHealingRequest(brokenLocator, null, null, attributes, null);
    }

    public String getBrokenLocator() { return brokenLocator; }
    public LocatorStrategy getBrokenStrategy() { return brokenStrategy; }
    public String getElementDescription() { return elementDescription; }
    public Map<String, String> getAttributes() { return attributes; }
    public String getCorrelationId() { return correlationId; }
}
