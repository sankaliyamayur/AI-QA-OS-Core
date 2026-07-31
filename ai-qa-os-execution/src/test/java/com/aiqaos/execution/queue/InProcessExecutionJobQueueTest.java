package com.aiqaos.execution.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.execution.engine.ExecutionConfiguration;
import com.aiqaos.execution.engine.ExecutionEngine;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** SCALE-1: in-JVM worker-pool submit → run → await round-trip. Hand-written stub engine, no Mockito. */
class InProcessExecutionJobQueueTest {

    /** Stub engine for a given framework that returns a canned result (or throws). */
    private static ExecutionEngine engine(String framework, boolean fail) {
        return new ExecutionEngine() {
            @Override
            public String getSupportedFramework() {
                return framework;
            }

            @Override
            public ExecutionResult execute(GeneratedScriptSuite scriptSuite, ExecutionConfiguration configuration) {
                if (fail) {
                    throw new RuntimeException("engine exploded");
                }
                ExecutionResult result = new ExecutionResult();
                result.setSuccess(true);
                result.setStatus("PASSED");
                return result;
            }

            @Override
            public void cancel() {
            }

            @Override
            public boolean isRunning() {
                return false;
            }
        };
    }

    private static ExecutionJob job(String framework) {
        return new ExecutionJob(UUID.randomUUID().toString(), "wf", "ex", "corr", framework, null, null);
    }

    @Test
    void submitRunsOnAWorkerAndReturnsTheResult() {
        InProcessExecutionJobQueue queue =
                new InProcessExecutionJobQueue(new ExecutionJobRunner(new ExecutionEngineFactory(List.of(engine("TEST", false)))));

        ExecutionJob job = job("TEST");
        queue.submit(job);
        ExecutionJobResult result = queue.awaitResult(job.getJobId(), Duration.ofSeconds(5));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isNotNull();
        assertThat(result.getResult().getStatus()).isEqualTo("PASSED");
    }

    @Test
    void engineFailureBecomesAFailedResultNotAThrow() {
        InProcessExecutionJobQueue queue =
                new InProcessExecutionJobQueue(new ExecutionJobRunner(new ExecutionEngineFactory(List.of(engine("BOOM", true)))));

        ExecutionJob job = job("BOOM");
        queue.submit(job);
        ExecutionJobResult result = queue.awaitResult(job.getJobId(), Duration.ofSeconds(5));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getResult()).isNull();
        assertThat(result.getErrorMessage()).contains("engine exploded");
    }

    @Test
    void awaitingAnUnknownJobFails() {
        InProcessExecutionJobQueue queue =
                new InProcessExecutionJobQueue(new ExecutionJobRunner(new ExecutionEngineFactory(List.of(engine("TEST", false)))));

        assertThatThrownBy(() -> queue.awaitResult("no-such-job", Duration.ofSeconds(1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
