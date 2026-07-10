package com.aiqaos.core.provider;

public interface VisionProvider {
    String analyzeImage(byte[] imageBytes, String prompt);
}