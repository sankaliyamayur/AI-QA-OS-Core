package com.aiqaos.memory.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.ArrayList;
import java.util.List;

public class MemorySearchResult implements BaseDTO {
    private List<MemoryNodeDTO> nodes = new ArrayList<>();
    private List<Double> relevanceScores = new ArrayList<>();

    public List<MemoryNodeDTO> getNodes() { return nodes; }
    public void setNodes(List<MemoryNodeDTO> nodes) { this.nodes = nodes; }
    public List<Double> getRelevanceScores() { return relevanceScores; }
    public void setRelevanceScores(List<Double> relevanceScores) { this.relevanceScores = relevanceScores; }
}