package com.aiqaos.workflow.context;

import com.aiqaos.core.context.WorkflowContext;

public class WorkflowContextHolder {
    private static final ThreadLocal<WorkflowContext> threadLocalContext = new ThreadLocal<>();

    public static void setContext(WorkflowContext context) {
        threadLocalContext.set(context);
    }

    public static WorkflowContext getContext() {
        return threadLocalContext.get();
    }

    public static void clearContext() {
        threadLocalContext.remove();
    }
}