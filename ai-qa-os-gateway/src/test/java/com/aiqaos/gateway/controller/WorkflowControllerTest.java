package com.aiqaos.gateway.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.aiqaos.gateway.dto.HumanReviewDTO;
import com.aiqaos.gateway.dto.ReviewDecisionDTO;
import com.aiqaos.gateway.dto.WorkflowResponseDTO;
import com.aiqaos.gateway.service.WorkflowGatewayService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

/**
 * MNT-3 gateway coverage — {@code WorkflowController} delegation + request-shaping logic.
 *
 * <p>Design §0.4 chose {@code @WebMvcTest}, but the gateway's slice context proved brittle in this
 * JDK-25 environment (its {@code Filter} @Components need security beans absent from the slice, and
 * {@code @MockBean} — the usual fix — needs Mockito, which is unusable here). Per the approved
 * fallback, these are plain unit tests over the controller's own logic with a hand-written stub
 * service. The HTTP wiring itself is standard Spring MVC; see {@link GlobalExceptionHandlerTest}
 * for the error contract.
 */
class WorkflowControllerTest {

    private final StubWorkflowGatewayService service = new StubWorkflowGatewayService();
    private final WorkflowController controller = new WorkflowController(service);

    @Test
    void getStatusDelegatesAndReturnsOk() {
        ResponseEntity<WorkflowResponseDTO> response = controller.getStatus("wf-1");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getWorkflowId()).isEqualTo("wf-1");
        assertThat(response.getBody().getWorkflowStatus()).isEqualTo("RUNNING");
    }

    @Test
    void reviewsReturnsTheServiceList() {
        ResponseEntity<List<HumanReviewDTO>> response = controller.reviews();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void approveExtractsReviewerAndCommentFromDecision() {
        ReviewDecisionDTO decision = new ReviewDecisionDTO();
        decision.setReviewer("alice");
        decision.setComment("looks good");

        controller.approve("wf-1", decision);

        assertThat(service.lastReviewer).isEqualTo("alice");
        assertThat(service.lastComment).isEqualTo("looks good");
    }

    @Test
    void approveToleratesAMissingDecisionBody() {
        // @RequestBody(required=false): a null body must not NPE and forwards null reviewer/comment.
        controller.approve("wf-1", null);

        assertThat(service.lastReviewer).isNull();
        assertThat(service.lastComment).isNull();
    }

    /** Hand-written stub (no Mockito); records the last approve/reject args. */
    static class StubWorkflowGatewayService extends WorkflowGatewayService {
        String lastReviewer;
        String lastComment;

        StubWorkflowGatewayService() {
            super(null, null, null);
        }

        @Override
        public WorkflowResponseDTO getStatus(String workflowId) {
            WorkflowResponseDTO dto = new WorkflowResponseDTO();
            dto.setWorkflowId(workflowId);
            dto.setWorkflowStatus("RUNNING");
            return dto;
        }

        @Override
        public List<HumanReviewDTO> listReviews() {
            return List.of(new HumanReviewDTO());
        }

        @Override
        public WorkflowResponseDTO approve(String id, String reviewer, String comment) {
            this.lastReviewer = reviewer;
            this.lastComment = comment;
            return new WorkflowResponseDTO();
        }

        @Override
        public WorkflowResponseDTO reject(String id, String reviewer, String comment) {
            this.lastReviewer = reviewer;
            this.lastComment = comment;
            return new WorkflowResponseDTO();
        }
    }
}
