package com.aiqaos.core.contract;

public class MemoryRequest extends BaseRequest {
    private String query;
    private int maxElements;

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public int getMaxElements() { return maxElements; }
    public void setMaxElements(int maxElements) { this.maxElements = maxElements; }
}