package com.aiqaos.reporting.repository;

import com.aiqaos.reporting.entity.TrendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface TrendRepository extends JpaRepository<TrendEntity, UUID> {
}