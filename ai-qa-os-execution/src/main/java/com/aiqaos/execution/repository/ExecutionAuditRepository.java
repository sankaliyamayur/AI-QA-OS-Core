package com.aiqaos.execution.repository;

import com.aiqaos.execution.entity.ExecutionAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ExecutionAuditRepository extends JpaRepository<ExecutionAuditEntity, UUID> {
}