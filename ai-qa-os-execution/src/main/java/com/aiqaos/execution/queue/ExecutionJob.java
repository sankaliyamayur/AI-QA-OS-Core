package com.aiqaos.execution.queue;

import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.execution.engine.ExecutionConfiguration;

/**
 * SCALE-1: a unit of execution work placed on the {@link ExecutionJobQueue}. Carries everything a
 * worker needs to run the scripts, so the worker can live in another thread, process, or (later,
 * behind a real broker) another host.
 *
 * <p>Lives in {@code ai-qa-os-execution} rather than {@code core} because it references
 * {@link ExecutionConfiguration} (an execution-module type); {@code core} cannot depend on
 * {@code execution}. Orchestration (the producer) already depends on {@code execution}. (ADR-017.)
 */
public class ExecutionJob {

    private final String jobId;
    private final String workflowId;
    private final String executionId;
    private final String correlationId;
    private final String framework;
    private final GeneratedScriptSuite scriptSuite;
    private final ExecutionConfiguration configuration;

    public ExecutionJob(String jobId, String workflowId, String executionId, String correlationId,
                        String framework, GeneratedScriptSuite scriptSuite,
                        ExecutionConfiguration configuration) {
        this.jobId = jobId;
        this.workflowId = workflowId;
        this.executionId = executionId;
        this.correlationId = correlationId;
        this.framework = framework;
        this.scriptSuite = scriptSuite;
        this.configuration = configuration;
    }

    public String getJobId() {
        return jobId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getFramework() {
        return framework;
    }

    public GeneratedScriptSuite getScriptSuite() {
        return scriptSuite;
    }

    public ExecutionConfiguration getConfiguration() {
        return configuration;
    }
}
