package com.aiqaos.execution.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.execution.engine.BrowserType;
import com.aiqaos.execution.engine.ExecutionConfiguration;
import com.aiqaos.execution.engine.ExecutionEngine;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
import com.aiqaos.execution.engine.ExecutionMode;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/** WF-4: the scheduler fans a matrix out to the engine and aggregates. Hand-written stub engine. */
class ShardedExecutionSchedulerTest {

    /** Records each unit config it is asked to run (thread-safe for the parallel path). */
    static class RecordingEngine implements ExecutionEngine {
        final List<String> calls = Collections.synchronizedList(new java.util.ArrayList<>());

        @Override
        public String getSupportedFramework() {
            return "Playwright";
        }

        @Override
        public ExecutionResult execute(GeneratedScriptSuite scriptSuite, ExecutionConfiguration c) {
            calls.add(c.getBrowser() + "#" + c.getShardIndex() + "/" + c.getShardCount());
            ExecutionResult r = new ExecutionResult();
            r.setSuccess(true);
            r.setStatus("PASSED");
            r.setPassed(1);
            r.setDuration(10);
            return r;
        }

        @Override
        public void cancel() {
        }

        @Override
        public boolean isRunning() {
            return false;
        }
    }

    private ShardedExecutionScheduler schedulerWith(RecordingEngine engine) {
        return new ShardedExecutionScheduler(
                new ExecutionEngineFactory(List.of(engine)),
                new ExecutionMatrixPlanner(),
                new ExecutionResultAggregator());
    }

    @Test
    void defaultConfigRunsExactlyOneUnit() {
        RecordingEngine engine = new RecordingEngine();
        ExecutionResult result = schedulerWith(engine).execute(new GeneratedScriptSuite(), new ExecutionConfiguration(), "Playwright");

        assertThat(engine.calls).hasSize(1);
        assertThat(engine.calls.get(0)).isEqualTo("CHROME#0/1");   // no fan-out, no shard
        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    void parallelMatrixFansOutToEveryBrowserShardAndAggregates() {
        RecordingEngine engine = new RecordingEngine();
        ExecutionConfiguration config = new ExecutionConfiguration();
        config.setBrowsers(List.of(BrowserType.CHROME, BrowserType.FIREFOX));
        config.setShardCount(2);
        config.setExecutionMode(ExecutionMode.PARALLEL);

        ExecutionResult result = schedulerWith(engine).execute(new GeneratedScriptSuite(), config, "Playwright");

        assertThat(engine.calls).hasSize(4);
        assertThat(engine.calls).containsExactlyInAnyOrder(
                "CHROME#1/2", "CHROME#2/2", "FIREFOX#1/2", "FIREFOX#2/2");
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getPassed()).isEqualTo(4);   // aggregated across the 4 units
    }

    @Test
    void sequentialMatrixRunsAllUnits() {
        RecordingEngine engine = new RecordingEngine();
        ExecutionConfiguration config = new ExecutionConfiguration();
        config.setBrowsers(List.of(BrowserType.CHROME, BrowserType.FIREFOX));
        config.setShardCount(1);
        config.setExecutionMode(ExecutionMode.SEQUENTIAL);

        ExecutionResult result = schedulerWith(engine).execute(new GeneratedScriptSuite(), config, "Playwright");

        assertThat(engine.calls).containsExactly("CHROME#0/1", "FIREFOX#0/1");
        assertThat(result.getPassed()).isEqualTo(2);
    }
}
