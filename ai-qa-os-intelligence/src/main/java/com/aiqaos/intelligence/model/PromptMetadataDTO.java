package com.aiqaos.intelligence.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.HashMap;
import java.util.Map;

public class PromptMetadataDTO implements BaseDTO {
    private String targetModel;
    private String author;
    private Map<String, String> tags = new HashMap<>();

    public String getTargetModel() { return targetModel; }
    public void setTargetModel(String targetModel) { this.targetModel = targetModel; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Map<String, String> getTags() { return tags; }
    public void setTags(Map<String, String> tags) { this.tags = tags; }
}