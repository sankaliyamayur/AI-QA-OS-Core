package com.aiqaos.execution.artifact;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * SCALE-1 single-host reference {@link ArtifactStore}: keys map to files under the artifact base
 * directory (the existing {@code playwright-output/}). Keys are validated so they cannot escape the
 * base dir. ENT-5 adds an object-storage impl behind the same seam ({@link ObjectStorageArtifactStore}).
 *
 * <p>Default store (active unless {@code aiqaos.artifacts.store=object}).
 */
@Component
@ConditionalOnProperty(name = "aiqaos.artifacts.store", havingValue = "local", matchIfMissing = true)
public class LocalArtifactStore implements ArtifactStore {

    private final Path baseDir;

    public LocalArtifactStore(@Value("${aiqaos.artifacts.base-dir:./playwright-output}") String baseDir) {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(String key, byte[] content) {
        Path target = safeResolve(key);
        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.write(target, content);
            return key;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store artifact: " + key, e);
        }
    }

    @Override
    public byte[] resolve(String key) {
        try {
            return Files.readAllBytes(safeResolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to resolve artifact: " + key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        return Files.exists(safeResolve(key));
    }

    @Override
    public List<String> list(String prefix) {
        if (!Files.exists(baseDir)) {
            return List.of();
        }
        try (Stream<Path> walk = Files.walk(baseDir)) {
            return walk.filter(Files::isRegularFile)
                    .map(p -> baseDir.relativize(p).toString().replace('\\', '/'))
                    .filter(key -> prefix == null || prefix.isEmpty() || key.startsWith(prefix))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to list artifacts under: " + prefix, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(safeResolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete artifact: " + key, e);
        }
    }

    @Override
    public Instant lastModified(String key) {
        try {
            return Files.getLastModifiedTime(safeResolve(key)).toInstant();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to stat artifact: " + key, e);
        }
    }

    /** Resolve a key under the base dir, rejecting traversal/absolute keys. */
    private Path safeResolve(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Artifact key must not be blank");
        }
        Path resolved = baseDir.resolve(key).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new IllegalArgumentException("Illegal artifact key (escapes base dir): " + key);
        }
        return resolved;
    }
}
