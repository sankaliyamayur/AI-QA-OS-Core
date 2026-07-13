package com.aiqaos.execution.component;

import org.springframework.stereotype.Component;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ExecutionResourceMonitor {
    private final AtomicInteger activeBrowsers = new AtomicInteger(0);
    private final AtomicInteger activeDrivers = new AtomicInteger(0);

    public void incrementBrowsers() { activeBrowsers.incrementAndGet(); }
    public void decrementBrowsers() { activeBrowsers.decrementAndGet(); }
    public void incrementDrivers() { activeDrivers.incrementAndGet(); }
    public void decrementDrivers() { activeDrivers.decrementAndGet(); }

    public double getCpuUsage() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return osBean.getCpuLoad() * 100.0;
    }

    public long getFreeMemory() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return osBean.getFreeMemorySize();
    }

    public int getActiveBrowsers() { return activeBrowsers.get(); }
    public int getActiveDrivers() { return activeDrivers.get(); }
}
