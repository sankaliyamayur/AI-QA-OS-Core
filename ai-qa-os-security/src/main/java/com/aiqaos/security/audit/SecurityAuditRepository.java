package com.aiqaos.security.audit;

import com.aiqaos.security.audit.SecurityAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SecurityAuditRepository extends JpaRepository<SecurityAuditEntity, UUID> {

    // GOV-1: all security events for a run (audit trail). workflowId is stored as a String here.
    List<SecurityAuditEntity> findByWorkflowId(String workflowId);
}