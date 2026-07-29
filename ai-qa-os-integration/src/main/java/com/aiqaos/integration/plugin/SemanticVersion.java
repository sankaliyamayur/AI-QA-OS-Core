package com.aiqaos.integration.plugin;

import java.util.Objects;

/**
 * PLG-1: a minimal semantic version ({@code major.minor.patch}) with the compatibility rule the
 * plugin runtime enforces: a plugin targeting SDK API {@code X.Y} is compatible with a runtime of the
 * <b>same major</b> whose <b>minor ≥ Y</b> (newer-but-compatible runtime accepts older-minor plugins;
 * a plugin needing a newer minor than the runtime is refused; a different major is incompatible).
 */
public final class SemanticVersion {

    private final int major;
    private final int minor;
    private final int patch;

    public SemanticVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static SemanticVersion parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("version is blank");
        }
        String[] parts = text.trim().split("\\.");
        if (parts.length < 1 || parts.length > 3) {
            throw new IllegalArgumentException("invalid semantic version: " + text);
        }
        try {
            int ma = Integer.parseInt(parts[0]);
            int mi = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int pa = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            return new SemanticVersion(ma, mi, pa);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid semantic version: " + text, e);
        }
    }

    /** True if a plugin targeting {@code this} version can run on the given {@code runtime}. */
    public boolean isCompatibleWith(SemanticVersion runtime) {
        return runtime != null && this.major == runtime.major && runtime.minor >= this.minor;
    }

    public int getMajor() { return major; }
    public int getMinor() { return minor; }
    public int getPatch() { return patch; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SemanticVersion v)) return false;
        return major == v.major && minor == v.minor && patch == v.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}
