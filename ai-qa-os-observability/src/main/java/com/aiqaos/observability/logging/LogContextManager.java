package com.aiqaos.observability.logging;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class LogContextManager {

    public void put(String key, String val) {
        MDC.put(key, val);
    }

    public void remove(String key) {
        MDC.remove(key);
    }

    public void clear() {
        MDC.clear();
    }
}