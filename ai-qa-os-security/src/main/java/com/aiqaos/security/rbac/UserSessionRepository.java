package com.aiqaos.security.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSessionEntity, UUID> {
    Optional<UserSessionEntity> findBySessionId(UUID sessionId);
    Optional<UserSessionEntity> findByRefreshToken(String refreshToken);
}
