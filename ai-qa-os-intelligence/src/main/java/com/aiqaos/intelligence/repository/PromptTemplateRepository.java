package com.aiqaos.intelligence.repository;

import com.aiqaos.intelligence.entity.PromptTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplateEntity, UUID> {
    Optional<PromptTemplateEntity> findByName(String name);
}