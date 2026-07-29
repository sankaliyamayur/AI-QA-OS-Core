package com.aiqaos.execution.scheduler;

import com.aiqaos.core.model.ExecutionResult;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * WF-4: merges the per-unit ({@code browser × shard}) results of a matrix run into one suite result.
 * Success requires every unit to pass; counts and durations sum; artifacts/screenshots union; the
 * first failure's message is surfaced.
 */
@Component
public class ExecutionResultAggregator {

    public ExecutionResult merge(List<ExecutionResult> units) {
        ExecutionResult aggregate = new ExecutionResult();
        aggregate.setAgentId("execution-engineer-playwright");

        if (units == null || units.isEmpty()) {
            aggregate.setSuccess(false);
            aggregate.setStatus("NO_RESULTS");
            return aggregate;
        }

        boolean allSucceeded = units.stream().allMatch(ExecutionResult::isSuccess);
        aggregate.setSuccess(allSucceeded);
        aggregate.setStatus(allSucceeded ? "PASSED" : "FAILED");

        int passed = 0;
        int failed = 0;
        int skipped = 0;
        long duration = 0;
        for (ExecutionResult unit : units) {
            passed += unit.getPassed();
            failed += unit.getFailed();
            skipped += unit.getSkipped();
            duration += unit.getDuration();
            if (unit.getScreenshots() != null) {
                aggregate.getScreenshots().addAll(unit.getScreenshots());
            }
            if (unit.getArtifacts() != null) {
                aggregate.getArtifacts().addAll(unit.getArtifacts());
            }
            if (!unit.isSuccess() && unit.getErrorMessage() != null && aggregate.getErrorMessage() == null) {
                aggregate.setErrorMessage(unit.getErrorMessage());
            }
        }
        aggregate.setPassed(passed);
        aggregate.setFailed(failed);
        aggregate.setSkipped(skipped);
        aggregate.setDuration(duration);
        return aggregate;
    }
}
