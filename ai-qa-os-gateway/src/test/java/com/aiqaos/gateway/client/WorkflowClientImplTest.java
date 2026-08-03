package com.aiqaos.gateway.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.UUID;

import com.aiqaos.gateway.dto.WorkflowResponseDTO;
import com.aiqaos.gateway.dto.WorkflowStartRequestDTO;
import com.aiqaos.orchestration.pipeline.AutonomousQAPipelineOrchestrator;
import com.aiqaos.orchestration.review.HumanReviewService;
import org.junit.jupiter.api.Test;

/**
 * Regression (live E2E): {@link WorkflowClientImpl#start} must not NPE when the request omits a
 * correlationId. Previously the metadata correlationId was only set when the request supplied one,
 * so the response's {@code correlationId.toString()} threw and the endpoint returned 500.
 */
class WorkflowClientImplTest {

    private final AutonomousQAPipelineOrchestrator orchestrator = mock(AutonomousQAPipelineOrchestrator.class);
    private final HumanReviewService humanReviewService = mock(HumanReviewService.class);
    private final WorkflowClientImpl client = new WorkflowClientImpl(orchestrator, humanReviewService);

    @Test
    void startWithoutCorrelationIdDefaultsOneAndDoesNotThrow() {
        WorkflowStartRequestDTO request = new WorkflowStartRequestDTO();
        request.setWorkflowName("e2e-smoke");
        request.setParameters(Map.of("storyPath", "req.md", "llmModel", "simulator"));
        // correlationId intentionally left null.

        WorkflowResponseDTO[] holder = new WorkflowResponseDTO[1];
        assertThatCode(() -> holder[0] = client.start(request)).doesNotThrowAnyException();

        WorkflowResponseDTO response = holder[0];
        assertThat(response.getWorkflowStatus()).isEqualTo("STARTED");
        assertThat(response.getWorkflowId()).isNotBlank();
        // A valid UUID string was returned, not an NPE.
        assertThatCode(() -> UUID.fromString(response.getCorrelationId())).doesNotThrowAnyException();
    }
}
