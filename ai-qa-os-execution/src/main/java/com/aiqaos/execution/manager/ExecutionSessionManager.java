package com.aiqaos.execution.manager;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ExecutionSessionManager {
    private final Map<String, Object> sessions = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, Object sessionObject) {
        sessions.put(sessionId, sessionObject);
    }

    public Object getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void closeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public void closeAllSessions() {
        sessions.clear();
    }
}