package com.aiqaos.orchestration.pipeline;

import com.aiqaos.core.contract.WorkflowRequest;
import com.aiqaos.core.contract.WorkflowResponse;
import com.aiqaos.core.context.WorkflowContext;
import com.aiqaos.core.engine.WorkflowStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class AsyncPipelineStepExecutorTest {

    private AsyncPipelineStepExecutor asyncExecutor;

    @BeforeEach
    void setUp() {
        asyncExecutor = new AsyncPipelineStepExecutor();
    }

    @Test
    void testVirtualThreadStepExecutionSuccess() throws ExecutionException, InterruptedException {
        WorkflowStep<WorkflowRequest, WorkflowResponse> mockStep = new WorkflowStep<>() {
            @Override
            public String getName() {
                return "TEST_VIRTUAL_STEP";
            }

            @Override
            public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
                assertTrue(Thread.currentThread().isVirtual(), "Must execute on a Virtual Thread!");
                WorkflowResponse resp = new WorkflowResponse();
                resp.setStatus("SUCCESS");
                resp.setMessage("VT step executed");
                return resp;
            }
        };

        CompletableFuture<WorkflowResponse> future = asyncExecutor.executeStepAsync(mockStep, new WorkflowRequest(), new WorkflowContext());
        WorkflowResponse response = future.get();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("VT step executed", response.getMessage());
    }

    @Test
    void testVirtualThreadStepExecutionExceptionHandling() throws ExecutionException, InterruptedException {
        WorkflowStep<WorkflowRequest, WorkflowResponse> failingStep = new WorkflowStep<>() {
            @Override
            public String getName() {
                return "FAILING_VT_STEP";
            }

            @Override
            public WorkflowResponse execute(WorkflowRequest request, WorkflowContext context) {
                throw new RuntimeException("Simulated IO Timeout");
            }
        };

        CompletableFuture<WorkflowResponse> future = asyncExecutor.executeStepAsync(failingStep, new WorkflowRequest(), new WorkflowContext());
        WorkflowResponse response = future.get();

        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Simulated IO Timeout"));
    }
}
