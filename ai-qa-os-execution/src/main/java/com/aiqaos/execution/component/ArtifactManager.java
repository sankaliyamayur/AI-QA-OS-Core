package com.aiqaos.execution.component;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ArtifactManager {
    private final Map<String, List<String>> artifacts = new ConcurrentHashMap<>();

    public void addArtifact(String executionId, String filePath) {
        artifacts.computeIfAbsent(executionId, k -> new ArrayList<>()).add(filePath);
    }

    public List<String> getArtifacts(String executionId) {
        return artifacts.getOrDefault(executionId, new ArrayList<>());
    }
}