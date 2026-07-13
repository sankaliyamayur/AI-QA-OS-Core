package com.aiqaos.integration.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RequestPipeline {
    private static final Logger log = LoggerFactory.getLogger(RequestPipeline.class);

    public boolean validateRequest(String userStory) {
        log.info("Validating incoming request story string: length={}", 
            (userStory == null) ? 0 : userStory.length());
        return userStory != null && !userStory.trim().isEmpty();
    }
}