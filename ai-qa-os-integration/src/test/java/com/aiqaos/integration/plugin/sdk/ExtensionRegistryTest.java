package com.aiqaos.integration.plugin.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aiqaos.core.extension.Extension;
import com.aiqaos.core.extension.ExtensionKind;
import org.junit.jupiter.api.Test;

/** PLG-3: unit tests for the extension SDK registry — governed registration + discovery by kind. */
class ExtensionRegistryTest {

    private ExtensionRegistry registry() {
        ExtensionSdkProperties props = new ExtensionSdkProperties();
        props.setApiVersion("1.0.0");
        return new ExtensionRegistry(props);
    }

    /** A simple test extension. */
    private record TestExtension(String id, ExtensionKind kind, String extensionPoint,
                                 String sdkApiVersion) implements Extension {
        static TestExtension of(String id, ExtensionKind kind) {
            return new TestExtension(id, kind, kind + ":" + id, "1.0.0");
        }
    }

    @Test
    void registersExtensionsAcrossKinds() {
        ExtensionRegistry r = registry();
        r.register(TestExtension.of("selenium", ExtensionKind.EXECUTION_ENGINE));
        r.register(TestExtension.of("appium", ExtensionKind.EXECUTION_ENGINE));
        r.register(TestExtension.of("allure", ExtensionKind.REPORTER));

        assertThat(r.byKind(ExtensionKind.EXECUTION_ENGINE)).extracting(Extension::id)
                .containsExactlyInAnyOrder("selenium", "appium");
        assertThat(r.byKind(ExtensionKind.REPORTER)).extracting(Extension::id).containsExactly("allure");
        assertThat(r.kinds()).containsExactlyInAnyOrder(
                ExtensionKind.EXECUTION_ENGINE, ExtensionKind.REPORTER);
    }

    @Test
    void rejectsDuplicateIdWithinAKind() {
        ExtensionRegistry r = registry();
        r.register(TestExtension.of("selenium", ExtensionKind.EXECUTION_ENGINE));
        assertThatThrownBy(() -> r.register(TestExtension.of("selenium", ExtensionKind.EXECUTION_ENGINE)))
                .isInstanceOf(ExtensionRegistrationException.class)
                .hasMessageContaining("duplicate");
    }

    @Test
    void sameIdAcrossDifferentKindsIsAllowed() {
        ExtensionRegistry r = registry();
        r.register(TestExtension.of("chrome", ExtensionKind.BROWSER));
        r.register(TestExtension.of("chrome", ExtensionKind.AGENT)); // different kind → ok
        assertThat(r.find(ExtensionKind.BROWSER, "chrome")).isPresent();
        assertThat(r.find(ExtensionKind.AGENT, "chrome")).isPresent();
        assertThat(r.all()).hasSize(2);
    }

    @Test
    void rejectsIncompatibleSdkVersion() {
        ExtensionRegistry r = registry(); // runtime 1.0.0
        Extension future = new TestExtension("x", ExtensionKind.REPORTER, "x", "2.0.0"); // major mismatch
        assertThatThrownBy(() -> r.register(future))
                .isInstanceOf(ExtensionRegistrationException.class)
                .hasMessageContaining("incompatible");
    }

    @Test
    void findOnEmptyKindIsEmpty() {
        assertThat(registry().find(ExtensionKind.AGENT, "nope")).isEmpty();
        assertThat(registry().byKind(ExtensionKind.AGENT)).isEmpty();
    }
}
