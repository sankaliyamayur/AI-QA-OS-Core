package com.aiqaos.intelligence.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.UUID;

public class PromptTemplateDTO implements BaseDTO {
    private UUID id;
    private String name;
    private String description;
    private String activeVersion;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getActiveVersion() { return activeVersion; }
    public void setActiveVersion(String activeVersion) { this.activeVersion = activeVersion; }
}