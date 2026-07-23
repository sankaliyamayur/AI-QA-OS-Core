package com.aiqaos.orchestration.review;

import com.aiqaos.orchestration.entity.HumanReviewEntity;
import com.aiqaos.orchestration.repository.HumanReviewRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AI-2 — owns the durable human-review queue ({@link HumanReviewEntity}). The repository is optional
 * (via {@link ObjectProvider}) so unit tests without JPA can construct the service; when absent,
 * the queue is simply not persisted (the in-memory {@link PausedWorkflowRegistry} still drives resume).
 */
@Service
public class HumanReviewService {

    private final ObjectProvider<HumanReviewRepository> repositoryProvider;

    public HumanReviewService(ObjectProvider<HumanReviewRepository> repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    public void createPending(UUID workflowId, UUID executionId, String stepName, double confidence) {
        HumanReviewRepository repo = repositoryProvider.getIfAvailable();
        if (repo == null) {
            return;
        }
        HumanReviewEntity e = new HumanReviewEntity();
        e.setReviewId(UUID.randomUUID());
        e.setWorkflowId(workflowId);
        e.setExecutionId(executionId);
        e.setStepName(stepName);
        e.setConfidence(confidence);
        e.setStatus("PENDING");
        e.setCreatedTime(LocalDateTime.now());
        repo.save(e);
    }

    public void markApproved(UUID workflowId, String reviewer, String comment) {
        decide(workflowId, "APPROVED", reviewer, comment);
    }

    public void markRejected(UUID workflowId, String reviewer, String comment) {
        decide(workflowId, "REJECTED", reviewer, comment);
    }

    public List<HumanReviewEntity> listPending() {
        HumanReviewRepository repo = repositoryProvider.getIfAvailable();
        return repo == null ? List.of() : repo.findByStatusOrderByCreatedTimeDesc("PENDING");
    }

    private void decide(UUID workflowId, String status, String reviewer, String comment) {
        HumanReviewRepository repo = repositoryProvider.getIfAvailable();
        if (repo == null) {
            return;
        }
        repo.findFirstByWorkflowIdAndStatusOrderByCreatedTimeDesc(workflowId, "PENDING").ifPresent(e -> {
            e.setStatus(status);
            e.setReviewer(reviewer);
            e.setDecisionComment(comment);
            e.setDecidedTime(LocalDateTime.now());
            repo.save(e);
        });
    }
}
