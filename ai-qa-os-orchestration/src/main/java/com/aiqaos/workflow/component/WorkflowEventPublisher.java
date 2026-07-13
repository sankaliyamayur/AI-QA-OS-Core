package com.aiqaos.workflow.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class WorkflowEventPublisher {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void publish(Object event) {
        eventPublisher.publishEvent(event);
    }
}