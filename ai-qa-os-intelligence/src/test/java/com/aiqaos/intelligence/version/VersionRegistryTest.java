package com.aiqaos.intelligence.version;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * GOV-4: unit tests for the pin/rollback/history logic against the in-memory reference store. No
 * Spring, no Mockito (JDK-25 breaks byte-buddy) — pure logic.
 */
class VersionRegistryTest {

    private VersionRegistry newRegistry() {
        return new VersionRegistry(new InMemoryVersionPinStore());
    }

    private static final String KEY = "prompt:greeting";

    @Test
    void pinMakesVersionActive() {
        VersionRegistry reg = newRegistry();
        reg.pin(KEY, VersionKind.PROMPT, "v1", "alice");

        assertThat(reg.activeVersion(KEY)).contains("v1");
    }

    @Test
    void pinningANewVersionDeactivatesThePrior_singleActive() {
        VersionRegistry reg = newRegistry();
        reg.pin(KEY, VersionKind.PROMPT, "v1", "alice");
        reg.pin(KEY, VersionKind.PROMPT, "v2", "bob");

        assertThat(reg.activeVersion(KEY)).contains("v2");
        // exactly one active pin at a time
        long active = reg.history(KEY).stream().filter(VersionPin::isActive).count();
        assertThat(active).isEqualTo(1);
    }

    @Test
    void pinningTheAlreadyActiveVersionIsIdempotent() {
        VersionRegistry reg = newRegistry();
        reg.pin(KEY, VersionKind.PROMPT, "v1", "alice");
        reg.pin(KEY, VersionKind.PROMPT, "v1", "alice"); // no-op

        assertThat(reg.history(KEY)).hasSize(1);
        assertThat(reg.activeVersion(KEY)).contains("v1");
    }

    @Test
    void rollbackRevertsToPreviousDistinctVersion() {
        VersionRegistry reg = newRegistry();
        reg.pin(KEY, VersionKind.PROMPT, "v1", "alice");
        reg.pin(KEY, VersionKind.PROMPT, "v2", "bob"); // v2 now active, v1 is last-known-good

        Optional<VersionPin> rolled = reg.rollback(KEY, "carol");

        assertThat(rolled).isPresent();
        assertThat(rolled.get().getVersionTag()).isEqualTo("v1");
        assertThat(rolled.get().getActor()).isEqualTo("carol");
        assertThat(reg.activeVersion(KEY)).contains("v1"); // reverted
        // the rollback is itself an audited pin event
        assertThat(reg.history(KEY)).hasSize(3);
    }

    @Test
    void rollbackWithNoPriorVersionIsSafeNoOp() {
        VersionRegistry reg = newRegistry();
        // never pinned
        assertThat(reg.rollback(KEY, "carol")).isEmpty();

        // only one distinct version ever → nothing to roll back to
        reg.pin(KEY, VersionKind.PROMPT, "v1", "alice");
        assertThat(reg.rollback(KEY, "carol")).isEmpty();
        assertThat(reg.activeVersion(KEY)).contains("v1"); // unchanged
    }

    @Test
    void historyIsNewestFirstAndPerKey() {
        VersionRegistry reg = newRegistry();
        reg.pin(KEY, VersionKind.PROMPT, "v1", "alice");
        reg.pin(KEY, VersionKind.PROMPT, "v2", "bob");
        reg.pin("prompt:other", VersionKind.PROMPT, "x1", "dave"); // different key

        List<VersionPin> hist = reg.history(KEY);
        assertThat(hist).extracting(VersionPin::getVersionTag).containsExactly("v2", "v1");
        assertThat(reg.history("prompt:other")).extracting(VersionPin::getVersionTag)
                .containsExactly("x1");
    }

    @Test
    void registryIsGenericOverKind_modelVersionsModelledToo() {
        VersionRegistry reg = newRegistry();
        String modelKey = "model:default";
        reg.pin(modelKey, VersionKind.MODEL, "gpt-4o-2024-05", "alice");
        reg.pin(modelKey, VersionKind.MODEL, "gpt-4o-2024-08", "bob");

        assertThat(reg.activeVersion(modelKey)).contains("gpt-4o-2024-08");
        Optional<VersionPin> rolled = reg.rollback(modelKey, "carol");
        assertThat(rolled).isPresent();
        assertThat(rolled.get().getKind()).isEqualTo(VersionKind.MODEL); // kind preserved on rollback
        assertThat(reg.activeVersion(modelKey)).contains("gpt-4o-2024-05");
    }
}
