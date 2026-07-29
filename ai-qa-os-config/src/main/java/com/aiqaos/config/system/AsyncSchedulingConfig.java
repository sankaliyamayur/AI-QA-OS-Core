package com.aiqaos.config.system;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncSchedulingConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        return new TaskExecutorAdapter(
                Executors.newThreadPerTaskExecutor(
                        Thread.ofVirtual().name("ai-qa-os-async-vt-", 0).factory()
                )
        );
    }
}