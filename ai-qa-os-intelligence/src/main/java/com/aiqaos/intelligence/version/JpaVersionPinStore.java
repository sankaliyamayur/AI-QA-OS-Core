package com.aiqaos.intelligence.version;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * GOV-4: durable {@link VersionPinStore} over {@link VersionPinRepository}. Pins survive restarts,
 * making the registry the persistent authority on "what runs". Activated in production via
 * {@code aiqaos.version-registry.store=jpa}; the default deployment uses
 * {@link InMemoryVersionPinStore}.
 *
 * <p>Its live behaviour needs a running Postgres, so it is exercised in production rather than in
 * this environment's unit tests (which prove the same registry logic against the in-memory store).
 */
@Component
@ConditionalOnProperty(name = "aiqaos.version-registry.store", havingValue = "jpa")
public class JpaVersionPinStore implements VersionPinStore {

    private final VersionPinRepository repository;

    public JpaVersionPinStore(VersionPinRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void save(VersionPin pin) {
        if (pin.isActive()) {
            // Enforce the single-active invariant before inserting the new active pin.
            for (VersionPinEntity current : repository.findByRegistryKeyAndActivePinTrue(pin.getRegistryKey())) {
                current.setActivePin(false);
                repository.save(current);
            }
        }
        repository.save(toEntity(pin));
    }

    @Override
    public Optional<VersionPin> activePin(String registryKey) {
        return repository.findFirstByRegistryKeyAndActivePinTrueOrderByPinnedAtDesc(registryKey)
                .map(JpaVersionPinStore::toDomain);
    }

    @Override
    public List<VersionPin> history(String registryKey) {
        List<VersionPin> out = new ArrayList<>();
        for (VersionPinEntity e : repository.findByRegistryKeyOrderByPinnedAtDesc(registryKey)) {
            out.add(toDomain(e));
        }
        return out;
    }

    private static VersionPinEntity toEntity(VersionPin pin) {
        VersionPinEntity e = new VersionPinEntity();
        e.setRegistryKey(pin.getRegistryKey());
        e.setKind(pin.getKind());
        e.setVersionTag(pin.getVersionTag());
        e.setActor(pin.getActor());
        e.setPinnedAt(pin.getPinnedAt());
        e.setActivePin(pin.isActive());
        return e;
    }

    private static VersionPin toDomain(VersionPinEntity e) {
        return new VersionPin(e.getRegistryKey(), e.getKind(), e.getVersionTag(), e.getActor(),
                e.getPinnedAt(), e.isActivePin());
    }
}
