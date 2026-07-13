package com.aiqaos.security.repository;

import com.aiqaos.security.entity.SecurityAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SecurityAuditRepository extends JpaRepository<SecurityAuditEntity, UUID> {
}