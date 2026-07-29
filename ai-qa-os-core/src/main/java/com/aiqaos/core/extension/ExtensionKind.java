package com.aiqaos.core.extension;

/**
 * PLG-3: the platform's deepest extension points — the kinds of capability a third party can add via
 * the {@link Extension} SDK without touching core: a custom AI {@code AGENT}, a custom
 * {@code EXECUTION_ENGINE} (Selenium / REST-Assured / Appium beyond Playwright), a custom
 * {@code REPORTER}, or a {@code BROWSER}.
 */
public enum ExtensionKind {
    AGENT,
    EXECUTION_ENGINE,
    REPORTER,
    BROWSER
}
