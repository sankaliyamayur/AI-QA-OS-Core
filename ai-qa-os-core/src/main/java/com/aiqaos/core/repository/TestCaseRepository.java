package com.aiqaos.core.repository;

import com.aiqaos.core.entity.TestCaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCaseEntity, String> {
    List<TestCaseEntity> findByModuleId(String moduleId);
}
