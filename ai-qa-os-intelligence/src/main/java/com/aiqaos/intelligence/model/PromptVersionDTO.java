package com.aiqaos.intelligence.model;

import com.aiqaos.core.dto.BaseDTO;
import java.util.UUID;

public class PromptVersionDTO implements BaseDTO {
    private UUID id;
    private UUID templateId;
    private String version;
    private String content;
    private String author;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}