package com.aiqaos.execution.queue;

import java.time.Duration;

/**
 * SCALE-1: the seam that decouples the pipeline from the execution host. The producer
 * (orchestration's {@code ExecutionStep}) {@link #submit}s a job and {@link #awaitResult}s;
 * the worker runs the engine. {@link InProcessExecutionJobQueue} is the in-JVM reference; a
 * Redis-Streams-backed impl over containerised workers is the deferred distributed tier (ADR-017).
 *
 * <p>Submit-and-await decouples the execution <em>host</em>, not the pipeline's control flow —
 * a fully event-driven pipeline is SCALE-2 (FI-SCALE1-A).
 */
public interface ExecutionJobQueue {

    /** Enqueue a job for a worker to run; returns the job id. */
    String submit(ExecutionJob job);

    /** Block until the job completes or {@code timeout} elapses, then return its result. */
    ExecutionJobResult awaitResult(String jobId, Duration timeout);
}
