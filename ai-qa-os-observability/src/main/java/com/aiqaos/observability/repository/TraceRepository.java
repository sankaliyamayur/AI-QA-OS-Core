package com.aiqaos.observability.repository;

import com.aiqaos.observability.entity.TraceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface TraceRepository extends JpaRepository<TraceEntity, UUID> {
}