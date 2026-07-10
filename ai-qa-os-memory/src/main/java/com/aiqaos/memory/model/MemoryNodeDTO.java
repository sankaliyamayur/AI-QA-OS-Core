package com.aiqaos.memory.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryNodeDTO implements BaseDTO {
    private UUID id;
    private String content;
    private String memoryType;
    private String category;
    private String ownerId;
    private Map<String, String> metadata = new HashMap<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMemoryType() { return memoryType; }
    public void setMemoryType(String memoryType) { this.memoryType = memoryType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
}