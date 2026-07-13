package com.aiqaos.intelligence.loader;

import com.aiqaos.intelligence.entity.PromptTemplateEntity;
import com.aiqaos.intelligence.entity.PromptVersionEntity;
import com.aiqaos.intelligence.repository.PromptTemplateRepository;
import com.aiqaos.intelligence.repository.PromptVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class DbPromptSource implements PromptSource {

    @Autowired
    private PromptTemplateRepository templateRepository;

    @Autowired
    private PromptVersionRepository versionRepository;

    @Override
    public Optional<String> loadPrompt(String templateName, String version) {
        return templateRepository.findByName(templateName)
            .flatMap(tpl -> versionRepository.findByTemplateIdAndVersionTag(tpl.getId(), version))
            .map(PromptVersionEntity::getContent);
    }
}