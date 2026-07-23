package com.aiqaos.orchestration.repository;

import com.aiqaos.orchestration.entity.WorkflowStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStepEntity, UUID> {
}