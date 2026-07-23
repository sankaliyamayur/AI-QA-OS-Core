package com.aiqaos.orchestration.repository;

import com.aiqaos.orchestration.entity.HumanReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HumanReviewRepository extends JpaRepository<HumanReviewEntity, UUID> {

    List<HumanReviewEntity> findByStatusOrderByCreatedTimeDesc(String status);

    Optional<HumanReviewEntity> findFirstByWorkflowIdAndStatusOrderByCreatedTimeDesc(UUID workflowId, String status);
}
