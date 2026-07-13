package com.aiqaos.intelligence.manager;

import com.aiqaos.intelligence.entity.PromptTemplateEntity;
import com.aiqaos.intelligence.repository.PromptTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PromptVersionManager {

    @Autowired
    private PromptTemplateRepository templateRepository;

    public String getActiveVersion(String templateName) {
        return templateRepository.findByName(templateName)
            .map(PromptTemplateEntity::getActiveVersion)
            .orElse("latest");
    }
}