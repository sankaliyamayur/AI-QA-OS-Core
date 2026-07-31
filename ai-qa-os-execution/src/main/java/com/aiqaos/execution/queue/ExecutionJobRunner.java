package com.aiqaos.execution.queue;

import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.execution.engine.ExecutionEngine;
import com.aiqaos.execution.engine.ExecutionEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SCALE-1 (ADR-065): runs a single {@link ExecutionJob} on the resolved {@link ExecutionEngine} — the
 * shared work logic used by BOTH the in-process worker pool ({@link InProcessExecutionJobQueue}) and
 * the distributed Redis worker ({@code RedisStreamExecutionJobQueue}), so the run path is identical
 * regardless of transport. Never throws: an engine failure becomes an {@link ExecutionJobResult#failure}.
 */
@Component
public class ExecutionJobRunner {

    private static final Logger log = LoggerFactory.getLogger(ExecutionJobRunner.class);

    private final ExecutionEngineFactory engineFactory;

    public ExecutionJobRunner(ExecutionEngineFactory engineFactory) {
        this.engineFactory = engineFactory;
    }

    public ExecutionJobResult run(ExecutionJob job) {
        try {
            ExecutionEngine engine = engineFactory.getEngine(job.getFramework());
            ExecutionResult result = engine.execute(job.getScriptSuite(), job.getConfiguration());
            return ExecutionJobResult.success(job.getJobId(), result);
        } catch (Exception e) {
            log.error("Execution job {} failed: {}", job.getJobId(), e.toString(), e);
            return ExecutionJobResult.failure(job.getJobId(), e.getMessage());
        }
    }
}
