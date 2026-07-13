package com.aiqaos.memory.model;

public class VectorSearchResult {
    private MemoryNodeDTO node;
    private double similarity;
    private String collection;
    private MemoryMetadata metadata;

    public VectorSearchResult(MemoryNodeDTO node, double similarity, String collection, MemoryMetadata metadata) {
        this.node = node;
        this.similarity = similarity;
        this.collection = collection;
        this.metadata = metadata;
    }

    public MemoryNodeDTO getNode() { return node; }
    public void setNode(MemoryNodeDTO node) { this.node = node; }
    public double getSimilarity() { return similarity; }
    public void setSimilarity(double similarity) { this.similarity = similarity; }
    public String getCollection() { return collection; }
    public void setCollection(String collection) { this.collection = collection; }
    public MemoryMetadata getMetadata() { return metadata; }
    public void setMetadata(MemoryMetadata metadata) { this.metadata = metadata; }
}