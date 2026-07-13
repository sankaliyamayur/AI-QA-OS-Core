package com.aiqaos.execution.context;

import java.util.HashMap;
import java.util.Map;

public class ExecutionContextHolder {
    private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

    public static void set(String key, Object value) {
        context.get().put(key, value);
    }

    public static Object get(String key) {
        return context.get().get(key);
    }

    public static void clear() {
        context.remove();
    }
}