package com.aiqaos.integration.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** PLG-1: unit tests for semantic-version parsing + the runtime compatibility rule. */
class SemanticVersionTest {

    @Test
    void parsesMajorMinorPatch() {
        SemanticVersion v = SemanticVersion.parse("1.2.3");
        assertThat(v.getMajor()).isEqualTo(1);
        assertThat(v.getMinor()).isEqualTo(2);
        assertThat(v.getPatch()).isEqualTo(3);
    }

    @Test
    void parsesPartialVersions() {
        assertThat(SemanticVersion.parse("2")).isEqualTo(new SemanticVersion(2, 0, 0));
        assertThat(SemanticVersion.parse("2.5")).isEqualTo(new SemanticVersion(2, 5, 0));
    }

    @Test
    void compatibleWhenSameMajorAndRuntimeMinorAtLeastPluginMinor() {
        SemanticVersion runtime = SemanticVersion.parse("1.5.0");
        assertThat(SemanticVersion.parse("1.2.0").isCompatibleWith(runtime)).isTrue();  // older minor ok
        assertThat(SemanticVersion.parse("1.5.9").isCompatibleWith(runtime)).isTrue();  // same minor
    }

    @Test
    void incompatibleOnMajorMismatchOrNewerMinorThanRuntime() {
        SemanticVersion runtime = SemanticVersion.parse("1.5.0");
        assertThat(SemanticVersion.parse("2.0.0").isCompatibleWith(runtime)).isFalse(); // major mismatch
        assertThat(SemanticVersion.parse("1.6.0").isCompatibleWith(runtime)).isFalse(); // needs newer minor
    }

    @Test
    void rejectsBlankOrMalformed() {
        assertThatThrownBy(() -> SemanticVersion.parse("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SemanticVersion.parse("x.y")).isInstanceOf(IllegalArgumentException.class);
    }
}
