package com.aiqaos.brain.repository;

import com.aiqaos.brain.entity.LearningEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface LearningRepository extends JpaRepository<LearningEntity, UUID> {
}