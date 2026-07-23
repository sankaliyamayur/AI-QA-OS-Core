package com.aiqaos.execution.queue;

import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.execution.engine.ExecutionEngine;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * SCALE-1 in-JVM reference {@link ExecutionJobQueue}: a bounded worker pool that runs the resolved
 * {@link ExecutionEngine} off the caller's thread. Proves the decoupling single-host and is the
 * drop-in point for the deferred Redis-Streams / containerised-worker tier.
 *
 * <p><b>Opt-in</b> via {@code aiqaos.execution.queue.enabled=true}; when unset the bean is absent
 * and {@code ExecutionStep} keeps its current in-process path — so the default behaviour is
 * unchanged (non-breaking).
 */
@Component
@ConditionalOnProperty(name = "aiqaos.execution.queue.enabled", havingValue = "true")
public class InProcessExecutionJobQueue implements ExecutionJobQueue {

    private static final Logger log = LoggerFactory.getLogger(InProcessExecutionJobQueue.class);

    private final ExecutionEngineFactory engineFactory;
    private final ExecutorService pool;
    private final Map<String, CompletableFuture<ExecutionJobResult>> results = new ConcurrentHashMap<>();

    public InProcessExecutionJobQueue(ExecutionEngineFactory engineFactory,
                                      @Value("${aiqaos.execution.queue.workers:4}") int workers) {
        this.engineFactory = engineFactory;
        this.pool = Executors.newFixedThreadPool(Math.max(1, workers));
    }

    @Override
    public String submit(ExecutionJob job) {
        CompletableFuture<ExecutionJobResult> future = new CompletableFuture<>();
        results.put(job.getJobId(), future);
        pool.submit(() -> future.complete(runJob(job)));
        return job.getJobId();
    }

    private ExecutionJobResult runJob(ExecutionJob job) {
        try {
            ExecutionEngine engine = engineFactory.getEngine(job.getFramework());
            ExecutionResult result = engine.execute(job.getScriptSuite(), job.getConfiguration());
            return ExecutionJobResult.success(job.getJobId(), result);
        } catch (Exception e) {
            log.error("Execution job {} failed: {}", job.getJobId(), e.toString(), e);
            return ExecutionJobResult.failure(job.getJobId(), e.getMessage());
        }
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
