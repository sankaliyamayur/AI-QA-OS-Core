package com.aiqaos.eval.dataset;

import com.aiqaos.eval.contract.EvaluationCase;
import com.aiqaos.eval.contract.GoldenDatasetProvider;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Loads a golden suite from a classpath JSON resource ({@code golden/<suite>.json}). Versionable
 * and CI-friendly — the AI-3 harness's default source of cases. PE-1 adds memory-backed,
 * managed datasets behind the same {@link GoldenDatasetProvider} seam.
 */
@Component
public class ClasspathGoldenDatasetProvider implements GoldenDatasetProvider {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String basePath;

    public ClasspathGoldenDatasetProvider() {
        this("golden/");
    }

    public ClasspathGoldenDatasetProvider(String basePath) {
        this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
    }

    @Override
    public List<EvaluationCase> load(String suite) {
        String resource = basePath + suite + ".json";
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return List.of();
            }
            SuiteFile file = mapper.readValue(in, SuiteFile.class);
            if (file.cases == null) {
                return List.of();
            }
            List<EvaluationCase> cases = new ArrayList<>();
            for (CaseDto c : file.cases) {
                cases.add(new EvaluationCase(c.id, c.input, c.expectedOutput, c.criteria, c.tags));
            }
            return cases;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load golden suite: " + resource, e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class SuiteFile {
        public String suite;
        public List<CaseDto> cases;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class CaseDto {
        public String id;
        public String input;
        public String expectedOutput;
        public List<String> criteria;
        public List<String> tags;
    }
}
