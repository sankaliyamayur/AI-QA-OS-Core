package com.aiqaos.execution.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.model.ExecutionResult;
import java.util.List;
import org.junit.jupiter.api.Test;

/** WF-4: merging per-unit results into one suite result. */
class ExecutionResultAggregatorTest {

    private final ExecutionResultAggregator aggregator = new ExecutionResultAggregator();

    private static ExecutionResult unit(boolean success, int passed, int failed, String artifact) {
        ExecutionResult r = new ExecutionResult();
        r.setSuccess(success);
        r.setPassed(passed);
        r.setFailed(failed);
        r.setDuration(100);
        r.getArtifacts().add(artifact);
        if (!success) {
            r.setErrorMessage("failed on " + artifact);
        }
        return r;
    }

    @Test
    void allUnitsPassingIsAPassingSuite() {
        ExecutionResult merged = aggregator.merge(List.of(
                unit(true, 3, 0, "chrome"), unit(true, 2, 0, "firefox")));

        assertThat(merged.isSuccess()).isTrue();
        assertThat(merged.getStatus()).isEqualTo("PASSED");
        assertThat(merged.getPassed()).isEqualTo(5);
        assertThat(merged.getDuration()).isEqualTo(200);
        assertThat(merged.getArtifacts()).containsExactlyInAnyOrder("chrome", "firefox");
    }

    @Test
    void anyFailingUnitFailsTheSuiteAndSurfacesTheError() {
        ExecutionResult merged = aggregator.merge(List.of(
                unit(true, 3, 0, "chrome"), unit(false, 1, 2, "firefox")));

        assertThat(merged.isSuccess()).isFalse();
        assertThat(merged.getStatus()).isEqualTo("FAILED");
        assertThat(merged.getFailed()).isEqualTo(2);
        assertThat(merged.getErrorMessage()).isEqualTo("failed on firefox");
    }

    @Test
    void emptyResultsIsNoResults() {
        ExecutionResult merged = aggregator.merge(List.of());

        assertThat(merged.isSuccess()).isFalse();
        assertThat(merged.getStatus()).isEqualTo("NO_RESULTS");
    }
}
