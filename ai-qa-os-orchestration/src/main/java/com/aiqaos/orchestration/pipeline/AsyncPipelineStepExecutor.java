package com.aiqaos.orchestration.pipeline;

import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.engine.WorkflowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AsyncPipelineStepExecutor {

    private static final Logger log = LoggerFactory.getLogger(AsyncPipelineStepExecutor.class);

    private final ExecutorService virtualThreadExecutor;

    public AsyncPipelineStepExecutor() {
        this.virtualThreadExecutor = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("pipeline-step-vt-", 0).factory()
        );
    }

    public CompletableFuture<WorkflowResponse> executeStepAsync(
            WorkflowStep<WorkflowRequest, WorkflowResponse> step,
            WorkflowRequest request,
            WorkflowContext context) {

        log.debug("PERF-1: Dispatching pipeline step '{}' to Virtual Thread", step.getName());

        return CompletableFuture.supplyAsync(() -> {
            try {
                return step.execute(request, context);
            } catch (Exception e) {
                log.error("PERF-1: Async execution of step '{}' failed", step.getName(), e);
                WorkflowResponse errResponse = new WorkflowResponse();
                errResponse.setStatus("FAILED");
                errResponse.setMessage("Async step failure: " + e.getMessage());
                return errResponse;
            }
        }, virtualThreadExecutor);
    }

    public ExecutorService getVirtualThreadExecutor() {
        return virtualThreadExecutor;
    }
}
