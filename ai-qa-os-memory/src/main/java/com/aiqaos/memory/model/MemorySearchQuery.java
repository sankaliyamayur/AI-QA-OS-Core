package com.aiqaos.memory.model;

import com.aiqaos.core.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

public class MemorySearchQuery implements BaseDTO {
    @NotBlank
    private String queryText;
    private int topK = 5;
    private double similarityThreshold = 0.7;
    private Map<String, Object> metadataFilters = new HashMap<>();

    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }
    public int getTopK() { return topK; }
    public void setTopK(int topK) { this.topK = topK; }
    public double getSimilarityThreshold() { return similarityThreshold; }
    public void setSimilarityThreshold(double similarityThreshold) { this.similarityThreshold = similarityThreshold; }
    public Map<String, Object> getMetadataFilters() { return metadataFilters; }
    public void setMetadataFilters(Map<String, Object> metadataFilters) { this.metadataFilters = metadataFilters; }
}