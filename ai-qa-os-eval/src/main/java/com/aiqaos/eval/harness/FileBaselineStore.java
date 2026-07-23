package com.aiqaos.eval.harness;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * File-backed {@link BaselineStore}: reads/writes {@code <baseDir>/<suite>.baseline.json}. The
 * committed-file baseline (ADR-013) — git-diffable and runnable in CI without a database. The
 * base directory is environment-specific; PE-1 wires it to the repo's {@code golden/} resources.
 */
public class FileBaselineStore implements BaselineStore {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path baseDir;

    public FileBaselineStore(Path baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public Baseline load(String suite) {
        Path file = baseDir.resolve(suite + ".baseline.json");
        if (!Files.exists(file)) {
            return new Baseline(suite, new HashMap<>());
        }
        try {
            return mapper.readValue(file.toFile(), Baseline.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read baseline: " + file, e);
        }
    }

    @Override
    public void save(Baseline baseline) {
        Path file = baseDir.resolve(baseline.getSuite() + ".baseline.json");
        try {
            Files.createDirectories(baseDir);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), baseline);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write baseline: " + file, e);
        }
    }
}
