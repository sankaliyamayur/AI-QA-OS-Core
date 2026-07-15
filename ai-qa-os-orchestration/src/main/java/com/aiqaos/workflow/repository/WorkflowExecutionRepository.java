package com.aiqaos.workflow.repository;

import com.aiqaos.workflow.entity.WorkflowExecutionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecutionEntity, UUID> {

    Optional<WorkflowExecutionEntity> findByExecutionId(UUID executionId);

    List<WorkflowExecutionEntity> findByWorkflowId(UUID workflowId);

    List<WorkflowExecutionEntity> findByStatus(String status);

    List<WorkflowExecutionEntity> findByGitBranch(String gitBranch);

    List<WorkflowExecutionEntity> findByGitCommit(String gitCommit);

    List<WorkflowExecutionEntity> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);

    List<WorkflowExecutionEntity> findByDurationBetween(long minDurationMs, long maxDurationMs);

    List<WorkflowExecutionEntity> findTop50ByOrderByStartTimeDesc();

    @Query("SELECT w FROM WorkflowExecutionEntity w WHERE " +
           "(:status IS NULL OR w.status = :status) AND " +
           "(:gitBranch IS NULL OR w.gitBranch = :gitBranch) AND " +
           "(:from IS NULL OR w.startTime >= :from) AND " +
           "(:to IS NULL OR w.startTime <= :to) " +
           "ORDER BY w.startTime DESC")
    Page<WorkflowExecutionEntity> search(@Param("status") String status,
                                          @Param("gitBranch") String gitBranch,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          Pageable pageable);
}
