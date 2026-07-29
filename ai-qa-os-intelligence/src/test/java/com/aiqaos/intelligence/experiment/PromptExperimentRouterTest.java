package com.aiqaos.intelligence.experiment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** PE-2: deterministic weighted A/B assignment. */
class PromptExperimentRouterTest {

    private final PromptExperimentRouter router = new PromptExperimentRouter();

    private static PromptExperiment experiment(boolean enabled, PromptExperiment.Variant... variants) {
        return new PromptExperiment("exp-1", "greeting", "v-default", List.of(variants), enabled);
    }

    @Test
    void sameKeyAlwaysGetsTheSameVariant() {
        PromptExperiment exp = experiment(true,
                new PromptExperiment.Variant("v-A", 50),
                new PromptExperiment.Variant("v-B", 50));

        String first = router.assign(exp, "wf-123");
        for (int i = 0; i < 20; i++) {
            assertThat(router.assign(exp, "wf-123")).isEqualTo(first);
        }
    }

    @Test
    void distributesRoughlyByWeightAcrossManyKeys() {
        PromptExperiment exp = experiment(true,
                new PromptExperiment.Variant("v-A", 80),
                new PromptExperiment.Variant("v-B", 20));

        int a = 0;
        int b = 0;
        for (int i = 0; i < 2000; i++) {
            String v = router.assign(exp, "key-" + i);
            if ("v-A".equals(v)) a++;
            else if ("v-B".equals(v)) b++;
        }
        // Both reachable; the 80-weight variant clearly dominates the 20-weight one.
        assertThat(a).isGreaterThan(b);
        assertThat(b).isGreaterThan(0);
        assertThat(a + b).isEqualTo(2000);
    }

    @Test
    void disabledOrEmptyExperimentReturnsDefault() {
        assertThat(router.assign(experiment(false, new PromptExperiment.Variant("v-A", 100)), "k"))
                .isEqualTo("v-default");
        assertThat(router.assign(experiment(true), "k")).isEqualTo("v-default");
        assertThat(router.assign(null, "k")).isNull();
    }
}
