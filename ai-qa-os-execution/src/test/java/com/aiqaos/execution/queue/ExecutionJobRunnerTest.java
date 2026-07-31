package com.aiqaos.execution.queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.execution.engine.ExecutionConfiguration;
import com.aiqaos.execution.engine.ExecutionEngine;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * SCALE-1 (ADR-065): the shared run logic used by both the in-process and Redis queues — wraps the
 * engine result, and turns an engine failure into a failure result (never throws). Mockito-free.
 */
class ExecutionJobRunnerTest {

    private ExecutionJob job() {
        return new ExecutionJob("j1", "wf", "ex", "corr", "playwright",
                new GeneratedScriptSuite(), new ExecutionConfiguration());
    }

    /** A fake {@link ExecutionEngine} whose execute() delegates to {@code exec}. */
    private ExecutionEngine engine(BiFunction<GeneratedScriptSuite, ExecutionConfiguration, ExecutionResult> exec) {
        return new ExecutionEngine() {
            @Override public String getSupportedFramework() { return "playwright"; }
            @Override public ExecutionResult execute(GeneratedScriptSuite s, ExecutionConfiguration c) { return exec.apply(s, c); }
            @Override public void cancel() { }
            @Override public boolean isRunning() { return false; }
        };
    }

    private ExecutionJobRunner runnerWith(ExecutionEngine engine) {
        ExecutionEngineFactory factory = new ExecutionEngineFactory(List.of()) {
            @Override
            public ExecutionEngine getEngine(String framework) {
                return engine;
            }
        };
        return new ExecutionJobRunner(factory);
    }

    @Test
    void run_success_wrapsEngineResult() {
        ExecutionResult engineResult = new ExecutionResult();
        ExecutionJobResult result = runnerWith(engine((s, c) -> engineResult)).run(job());

        assertTrue(result.isSuccess());
        assertEquals("j1", result.getJobId());
        assertSame(engineResult, result.getResult());
    }

    @Test
    void run_engineThrows_becomesFailureNotException() {
        Supplier<ExecutionResult> boom = () -> { throw new RuntimeException("boom"); };
        ExecutionJobResult result = runnerWith(engine((s, c) -> boom.get())).run(job());

        assertFalse(result.isSuccess());
        assertEquals("j1", result.getJobId());
        assertEquals("boom", result.getErrorMessage());
    }
}
