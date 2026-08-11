package com.aiqaos.execution.queue;

import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * SCALE-1 (ADR-065): the distributed {@link ExecutionJobQueue} over Redis Streams. {@code submit} XADDs
 * the JSON-serialised job to {@code execution:jobs}; a per-instance worker consumes via the shared
 * consumer group {@code execution-workers} (competing consumers → exactly one worker per job), runs it
 * through the shared {@link ExecutionJobRunner}, writes the result to {@code execution:result:<jobId>}
 * (TTL), and XACKs — so a worker on ANY instance can process any job. {@code awaitResult} polls the
 * result key, returning it to the original submitter across the pool.
 *
 * <p>At-least-once: unacked messages stay pending, and each worker recovers its OWN pending on startup
 * (crash-restart). Cross-instance reclaim of a permanently-dead worker's pending (XAUTOCLAIM) is a
 * documented follow-up. Active only when {@code aiqaos.execution.queue.enabled=true} AND
 * {@code provider=redis} AND {@code StringRedisTemplate} is on the classpath (an app opts in).
 */
@Component
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnExpression(
        "'${aiqaos.execution.queue.enabled:false}' == 'true' and '${aiqaos.execution.queue.provider:in-process}' == 'redis'")
public class RedisStreamExecutionJobQueue implements ExecutionJobQueue {

    private static final Logger log = LoggerFactory.getLogger(RedisStreamExecutionJobQueue.class);

    static final String STREAM = "execution:jobs";
    static final String GROUP = "execution-workers";
    static final String RESULT_PREFIX = "execution:result:";
    private static final String FIELD = "job";
    private static final Duration RESULT_TTL = Duration.ofMinutes(30);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(200);

    private final StringRedisTemplate redis;
    private final ExecutionJobRunner runner;
    private final ObjectMapper objectMapper;
    private final String consumerId;

    private volatile boolean groupReady = false;
    private volatile boolean running = true;
    private Thread worker;

    public RedisStreamExecutionJobQueue(StringRedisTemplate redis, ExecutionJobRunner runner,
                                        ObjectMapper objectMapper,
                                        @Value("${aiqaos.execution.queue.consumer-id:${random.uuid}}") String consumerId) {
        this.redis = redis;
        this.runner = runner;
        this.objectMapper = objectMapper;
        this.consumerId = consumerId;
    }

    @PostConstruct
    void start() {
        ensureGroup();
        recoverOwnPending();
        worker = new Thread(this::workerLoop, "redis-execution-worker-" + consumerId);
        worker.setDaemon(true);
        worker.start();
        log.info("Redis execution worker started (consumer={})", consumerId);
    }

    @Override
    public String submit(ExecutionJob job) {
        redis.opsForStream().add(STREAM, Map.of(FIELD, serialize(job)));
        ensureGroup();
        return job.getJobId();
    }

    @Override
    public ExecutionJobResult awaitResult(String jobId, Duration timeout) {
        String key = RESULT_PREFIX + jobId;
        long deadline = System.nanoTime() + timeout.toNanos();
        try {
            while (System.nanoTime() < deadline) {
                String json = redis.opsForValue().get(key);
                if (json != null) {
                    redis.delete(key);
                    return objectMapper.readValue(json, ExecutionJobResult.class);
                }
                Thread.sleep(POLL_INTERVAL.toMillis());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted awaiting execution job " + jobId, e);
        } catch (Exception e) {
            throw new IllegalStateException("Awaiting execution job " + jobId + " failed: " + e.getMessage(), e);
        }
        throw new IllegalStateException("Timed out awaiting execution job " + jobId);
    }

    // --- worker ---

    private void workerLoop() {
        while (running) {
            try {
                ensureGroup();
                List<MapRecord<String, Object, Object>> records = redis.opsForStream().read(
                        Consumer.from(GROUP, consumerId),
                        StreamReadOptions.empty().count(10).block(Duration.ofSeconds(2)),
                        StreamOffset.create(STREAM, ReadOffset.lastConsumed()));
                if (records != null) {
                    records.forEach(this::process);
                }
            } catch (Exception e) {
                if (running) {
                    log.debug("Worker read cycle failed (will retry): {}", e.toString());
                    sleepQuietly(Duration.ofSeconds(1));
                }
            }
        }
    }

    /** Recover this consumer's own unacked messages (crash-restart) — read from 0 = its pending list. */
    private void recoverOwnPending() {
        try {
            List<MapRecord<String, Object, Object>> pending = redis.opsForStream().read(
                    Consumer.from(GROUP, consumerId),
                    StreamReadOptions.empty().count(100),
                    StreamOffset.create(STREAM, ReadOffset.from("0")));
            if (pending != null && !pending.isEmpty()) {
                log.info("Recovering {} pending execution job(s) for consumer {}", pending.size(), consumerId);
                pending.forEach(this::process);
            }
        } catch (Exception e) {
            log.debug("No pending to recover: {}", e.toString());
        }
    }

    private void process(MapRecord<String, Object, Object> record) {
        try {
            Object raw = record.getValue().get(FIELD);
            if (raw == null) {
                redis.opsForStream().acknowledge(STREAM, GROUP, record.getId());
                return;
            }
            ExecutionJob job = objectMapper.readValue(String.valueOf(raw), ExecutionJob.class);
            ExecutionJobResult result = runner.run(job);
            redis.opsForValue().set(RESULT_PREFIX + job.getJobId(), serializeResult(result), RESULT_TTL);
            redis.opsForStream().acknowledge(STREAM, GROUP, record.getId());
        } catch (Exception e) {
            log.error("Failed to process execution record {}: {}", record.getId(), e.toString());
            // Left unacked → stays pending for redelivery (at-least-once).
        }
    }

    private void ensureGroup() {
        if (groupReady) {
            return;
        }
        try {
            redis.opsForStream().createGroup(STREAM, ReadOffset.from("0"), GROUP);
            groupReady = true;
        } catch (Exception e) {
            // BUSYGROUP = already exists → ready; otherwise the stream doesn't exist yet, retry later.
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                groupReady = true;
            }
        }
    }

    String serialize(ExecutionJob job) {
        try {
            return objectMapper.writeValueAsString(job);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise execution job " + job.getJobId(), e);
        }
    }

    String serializeResult(ExecutionJobResult result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise execution result " + result.getJobId(), e);
        }
    }

    private void sleepQuietly(Duration d) {
        try {
            Thread.sleep(d.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
    }
}
