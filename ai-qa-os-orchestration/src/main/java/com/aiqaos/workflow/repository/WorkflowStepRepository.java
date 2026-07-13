package com.aiqaos.workflow.repository;

import com.aiqaos.workflow.entity.WorkflowStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStepEntity, UUID> {
}