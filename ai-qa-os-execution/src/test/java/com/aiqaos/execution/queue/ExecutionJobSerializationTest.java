package com.aiqaos.execution.queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.core.model.ExecutionResult;
import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.execution.engine.ExecutionConfiguration;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * SCALE-1 (ADR-065): the Redis-backed queue serialises jobs/results as JSON, so both must round-trip
 * faithfully — ExecutionJob/ExecutionJobResult are immutable (@JsonCreator ctors) over no-arg-POJO
 * payloads. This is the contract the distributed queue depends on.
 */
class ExecutionJobSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void executionJob_roundTrips() throws Exception {
        ExecutionJob job = new ExecutionJob("job1", "wf1", "ex1", "corr1", "playwright",
                new GeneratedScriptSuite(), new ExecutionConfiguration());

        ExecutionJob back = mapper.readValue(mapper.writeValueAsString(job), ExecutionJob.class);

        assertEquals("job1", back.getJobId());
        assertEquals("wf1", back.getWorkflowId());
        assertEquals("playwright", back.getFramework());
        assertNotNull(back.getScriptSuite());
        assertNotNull(back.getConfiguration());
    }

    @Test
    void executionJobResult_roundTrips() throws Exception {
        ExecutionJobResult result = ExecutionJobResult.success("job1", new ExecutionResult());

        ExecutionJobResult back = mapper.readValue(mapper.writeValueAsString(result), ExecutionJobResult.class);

        assertEquals("job1", back.getJobId());
        assertTrue(back.isSuccess());
        assertNotNull(back.getResult());
    }

    @Test
    void failureResult_roundTrips() throws Exception {
        ExecutionJobResult result = ExecutionJobResult.failure("job2", "boom");

        ExecutionJobResult back = mapper.readValue(mapper.writeValueAsString(result), ExecutionJobResult.class);

        assertEquals("job2", back.getJobId());
        assertEquals(false, back.isSuccess());
        assertEquals("boom", back.getErrorMessage());
    }
}
