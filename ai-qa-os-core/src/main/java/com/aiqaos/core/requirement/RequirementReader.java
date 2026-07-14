package com.aiqaos.core.requirement;

import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class RequirementReader {

    public String readRequirement(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("Requirement file not found at path: " + filePath);
        }
        return Files.readString(path);
    }
}
