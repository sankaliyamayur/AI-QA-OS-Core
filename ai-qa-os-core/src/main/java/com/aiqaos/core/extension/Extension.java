package com.aiqaos.core.extension;

/**
 * PLG-3: the uniform extension SDK seam. A custom agent, execution engine, reporter, or browser
 * implements this to be discovered and governed by the runtime's {@code ExtensionRegistry}. Lives in
 * {@code core} so extenders in {@code agents}/{@code execution}/{@code reporting} (or third-party
 * modules depending on them) can implement it without a dependency cycle — the genuine cross-module
 * implementer case (ADR-010/015).
 */
public interface Extension {

    /** Stable unique id within the extension's {@link #kind()}. */
    String id();

    /** Which deep extension point this plugs into. */
    ExtensionKind kind();

    /** Human-readable description of what it extends (e.g. {@code "Selenium execution engine"}). */
    String extensionPoint();

    /** The SDK API version this extension targets (checked for compatibility at registration). */
    default String sdkApiVersion() {
        return "1.0.0";
    }
}
