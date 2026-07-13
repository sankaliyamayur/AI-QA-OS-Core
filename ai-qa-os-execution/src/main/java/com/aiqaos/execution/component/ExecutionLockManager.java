package com.aiqaos.execution.component;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ExecutionLockManager {
    private final Map<String, ReentrantLock> environmentLocks = new ConcurrentHashMap<>();

    public boolean acquireLock(String environment) {
        if (environment == null) return true;
        ReentrantLock lock = environmentLocks.computeIfAbsent(environment, k -> new ReentrantLock());
        return lock.tryLock();
    }

    public void releaseLock(String environment) {
        if (environment == null) return;
        ReentrantLock lock = environmentLocks.get(environment);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
