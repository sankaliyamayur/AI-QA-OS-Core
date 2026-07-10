package com.aiqaos.core.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileUtils {
    private FileUtils() {}

    public static boolean exists(String pathStr) {
        Path path = Paths.get(pathStr);
        return Files.exists(path);
    }
}