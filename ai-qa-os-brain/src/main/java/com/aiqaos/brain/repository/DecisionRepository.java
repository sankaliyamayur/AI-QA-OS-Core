package com.aiqaos.brain.repository;

import com.aiqaos.brain.entity.DecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DecisionRepository extends JpaRepository<DecisionEntity, UUID> {
}