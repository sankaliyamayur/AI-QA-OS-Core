package com.aiqaos.integration.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** PLG-1: unit tests for registration governance + the managed lifecycle. No Mockito. */
class PluginRegistryTest {

    private PluginRegistry registry;

    @BeforeEach
    void setup() {
        PluginProperties props = new PluginProperties();
        props.setSdkApiVersion("1.0.0");
        props.setGrantedPermissions(new LinkedHashSet<>(Set.of("read", "write")));
        registry = new PluginRegistry(props);
    }

    /** Records which lifecycle hooks were invoked. */
    private static final class RecordingPlugin implements Plugin {
        final String id;
        boolean initialized, enabled, disabled;
        RecordingPlugin(String id) { this.id = id; }
        public String id() { return id; }
        public void initialize(PluginContext c) { initialized = true; }
        public void onEnable() { enabled = true; }
        public void onDisable() { disabled = true; }
    }

    private PluginManifest manifest(String id, String sdkApi, Set<String> perms) {
        return new PluginManifest(id, SemanticVersion.parse("1.0.0"), SemanticVersion.parse(sdkApi),
                Set.of("cap-x"), perms);
    }

    @Test
    void registersCompatiblePluginWithGrantedPermissions() {
        PluginDescriptor d = registry.register(new RecordingPlugin("p1"),
                manifest("p1", "1.0.0", Set.of("read")));
        assertThat(d.getState()).isEqualTo(PluginState.REGISTERED);
        assertThat(registry.get("p1")).isPresent();
    }

    @Test
    void rejectsIncompatibleSdkVersion() {
        assertThatThrownBy(() -> registry.register(new RecordingPlugin("p2"),
                manifest("p2", "2.0.0", Set.of())))
                .isInstanceOf(PluginRegistrationException.class)
                .hasMessageContaining("incompatible");
    }

    @Test
    void rejectsUngrantedPermission() {
        assertThatThrownBy(() -> registry.register(new RecordingPlugin("p3"),
                manifest("p3", "1.0.0", Set.of("admin"))))
                .isInstanceOf(PluginRegistrationException.class)
                .hasMessageContaining("ungranted permission");
    }

    @Test
    void rejectsDuplicateId() {
        registry.register(new RecordingPlugin("dup"), manifest("dup", "1.0.0", Set.of()));
        assertThatThrownBy(() -> registry.register(new RecordingPlugin("dup"),
                manifest("dup", "1.0.0", Set.of())))
                .isInstanceOf(PluginRegistrationException.class)
                .hasMessageContaining("duplicate");
    }

    @Test
    void rejectsIdManifestMismatch() {
        assertThatThrownBy(() -> registry.register(new RecordingPlugin("a"),
                manifest("b", "1.0.0", Set.of())))
                .isInstanceOf(PluginRegistrationException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void drivesFullLifecycleAndInvokesHooks() {
        RecordingPlugin plugin = new RecordingPlugin("life");
        registry.register(plugin, manifest("life", "1.0.0", Set.of("read")));

        registry.initialize("life");
        assertThat(plugin.initialized).isTrue();
        assertThat(registry.get("life").get().getState()).isEqualTo(PluginState.INITIALIZED);

        registry.enable("life");
        assertThat(plugin.enabled).isTrue();
        assertThat(registry.enabled()).extracting(PluginDescriptor::getId).containsExactly("life");

        registry.disable("life");
        assertThat(plugin.disabled).isTrue();
        assertThat(registry.get("life").get().getState()).isEqualTo(PluginState.DISABLED);
        assertThat(registry.enabled()).isEmpty();
    }

    @Test
    void reEnableAfterDisableWorks() {
        RecordingPlugin plugin = new RecordingPlugin("re");
        registry.register(plugin, manifest("re", "1.0.0", Set.of()));
        registry.initialize("re");
        registry.enable("re");
        registry.disable("re");
        registry.enable("re"); // DISABLED → ENABLED
        assertThat(registry.get("re").get().getState()).isEqualTo(PluginState.ENABLED);
    }

    @Test
    void refusesInvalidTransitions() {
        registry.register(new RecordingPlugin("bad"), manifest("bad", "1.0.0", Set.of()));
        // enable before initialize
        assertThatThrownBy(() -> registry.enable("bad")).isInstanceOf(IllegalStateException.class);
        // disable before enable
        registry.initialize("bad");
        assertThatThrownBy(() -> registry.disable("bad")).isInstanceOf(IllegalStateException.class);
    }
}
