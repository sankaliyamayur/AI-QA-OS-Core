package com.aiqaos.execution.queue;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * SCALE-1 in-JVM reference {@link ExecutionJobQueue}: a bounded worker pool that runs the job off the
 * caller's thread via the shared {@link ExecutionJobRunner}. Proves the decoupling single-host; the
 * distributed tier is {@code RedisStreamExecutionJobQueue} (ADR-065).
 *
 * <p><b>Opt-in and default provider</b> (ADR-065): active only when {@code aiqaos.execution.queue.enabled=true}
 * AND {@code aiqaos.execution.queue.provider} is not {@code redis} (in-process is the default). When
 * queueing is off the bean is absent and {@code ExecutionStep} keeps its inline path — non-breaking.
 */
@Component
@ConditionalOnExpression(
        "'${aiqaos.execution.queue.enabled:false}' == 'true' and '${aiqaos.execution.queue.provider:in-process}' != 'redis'")
public class InProcessExecutionJobQueue implements ExecutionJobQueue {

    private static final Logger log = LoggerFactory.getLogger(InProcessExecutionJobQueue.class);

    private final ExecutionJobRunner runner;
    private final ExecutorService pool;
    private final Map<String, CompletableFuture<ExecutionJobResult>> results = new ConcurrentHashMap<>();

    public InProcessExecutionJobQueue(ExecutionJobRunner runner) {
        this.runner = runner;
        this.pool = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("execution-worker-vt-", 0).factory()
        );
    }

    @Override
    public String submit(ExecutionJob job) {
        CompletableFuture<ExecutionJobResult> future = new CompletableFuture<>();
        results.put(job.getJobId(), future);
        pool.submit(() -> future.complete(runner.run(job)));
        return job.getJobId();
    }

    @Override
    public ExecutionJobResult awaitResult(String jobId, Duration timeout) {
        CompletableFuture<ExecutionJobResult> future = results.get(jobId);
        if (future == null) {
            throw new IllegalStateException("No such execution job submitted: " + jobId);
        }
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Awaiting execution job " + jobId + " failed: " + e.getMessage(), e);
        } finally {
            results.remove(jobId);
        }
    }

    @PreDestroy
    public void shutdown() {
        pool.shutdownNow();
    }
}
