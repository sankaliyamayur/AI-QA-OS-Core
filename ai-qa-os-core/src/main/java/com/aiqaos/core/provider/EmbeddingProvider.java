package com.aiqaos.core.provider;

public interface EmbeddingProvider {
    float[] embed(String text);
}