package com.aiqaos.execution.repository;

import com.aiqaos.execution.entity.ExecutionArtifactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import java.util.List;

@Repository
public interface ExecutionArtifactRepository extends JpaRepository<ExecutionArtifactEntity, UUID> {
    List<ExecutionArtifactEntity> findByTestCaseId(String testCaseId);
    List<ExecutionArtifactEntity> findByTestCaseIdOrderByRunNumberDesc(String testCaseId);
}