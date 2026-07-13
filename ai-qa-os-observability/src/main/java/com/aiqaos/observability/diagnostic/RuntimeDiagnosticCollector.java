package com.aiqaos.observability.diagnostic;

import org.springframework.stereotype.Component;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

@Component
public class RuntimeDiagnosticCollector {

    public Map<String, Object> collectDiagnostics() {
        Map<String, Object> diag = new HashMap<>();
        
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        diag.put("heap_used_bytes", mem.getHeapMemoryUsage().getUsed());
        diag.put("heap_max_bytes", mem.getHeapMemoryUsage().getMax());

        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        diag.put("thread_count", threads.getThreadCount());

        // Platform-specific/virtual threads diagnostic summaries
        diag.put("active_browser_count", 0); 
        return diag;
    }
}