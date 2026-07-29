package com.aiqaos.intelligence.version;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * GOV-4: finders for durable version pins. Active-pin lookup and newest-first history back the
 * {@code JpaVersionPinStore} contract.
 */
@Repository
public interface VersionPinRepository extends JpaRepository<VersionPinEntity, UUID> {

    Optional<VersionPinEntity> findFirstByRegistryKeyAndActivePinTrueOrderByPinnedAtDesc(String registryKey);

    List<VersionPinEntity> findByRegistryKeyAndActivePinTrue(String registryKey);

    List<VersionPinEntity> findByRegistryKeyOrderByPinnedAtDesc(String registryKey);
}
