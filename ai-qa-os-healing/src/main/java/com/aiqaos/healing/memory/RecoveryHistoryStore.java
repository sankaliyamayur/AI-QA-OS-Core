package com.aiqaos.healing.memory;

import com.aiqaos.core.model.RecoveryAttempt;
import com.aiqaos.memory.store.MemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@SuppressWarnings("unchecked")
public class RecoveryHistoryStore {

    @Autowired
    private MemoryStore memoryStore;

    private static final String HISTORY_KEY_PREFIX = "healing:execution:";
    private static final Duration TTL = Duration.ofDays(30);

    public void storeAttempt(String executionId, RecoveryAttempt attempt) {
        String key = HISTORY_KEY_PREFIX + executionId;
        List<RecoveryAttempt> attempts = getAttempts(executionId);
        attempts.add(attempt);
        memoryStore.put(key, attempts, TTL);
    }

    public List<RecoveryAttempt> getAttempts(String executionId) {
        String key = HISTORY_KEY_PREFIX + executionId;
        Optional<Object> val = memoryStore.get(key);
        if (val.isPresent() && val.get() instanceof List) {
            return new ArrayList<>((List<RecoveryAttempt>) val.get());
        }
        return new ArrayList<>();
    }

    public void clear(String executionId) {
        String key = HISTORY_KEY_PREFIX + executionId;
        memoryStore.remove(key);
    }
}
