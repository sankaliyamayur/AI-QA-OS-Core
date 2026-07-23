package com.aiqaos.eval.repository;

import com.aiqaos.eval.entity.EvaluationResultEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence for {@link EvaluationResultEntity}. Optional at runtime — the service injects it
 * via {@code ObjectProvider} so evaluation works even where no JPA context is present.
 */
@Repository
public interface EvaluationResultRepository extends JpaRepository<EvaluationResultEntity, UUID> {

    List<EvaluationResultEntity> findBySuiteOrderByCreatedTimeDesc(String suite);
}
