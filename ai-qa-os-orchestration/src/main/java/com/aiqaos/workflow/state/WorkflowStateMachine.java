package com.aiqaos.workflow.state;

import com.aiqaos.core.exception.ValidationException;
import org.springframework.stereotype.Component;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class WorkflowStateMachine {
    private final Map<WorkflowState, Set<WorkflowState>> allowedTransitions = new HashMap<>();

    public WorkflowStateMachine() {
        allowedTransitions.put(WorkflowState.CREATED, EnumSet.of(WorkflowState.VALIDATED, WorkflowState.CANCELLED));
        allowedTransitions.put(WorkflowState.VALIDATED, EnumSet.of(WorkflowState.READY, WorkflowState.CANCELLED));
        allowedTransitions.put(WorkflowState.READY, EnumSet.of(WorkflowState.RUNNING, WorkflowState.CANCELLED));
        allowedTransitions.put(WorkflowState.RUNNING, EnumSet.of(WorkflowState.PAUSED, WorkflowState.COMPLETED, WorkflowState.FAILED, WorkflowState.CANCELLED));
        allowedTransitions.put(WorkflowState.PAUSED, EnumSet.of(WorkflowState.RESUMED, WorkflowState.CANCELLED));
        allowedTransitions.put(WorkflowState.RESUMED, EnumSet.of(WorkflowState.RUNNING, WorkflowState.CANCELLED));
        allowedTransitions.put(WorkflowState.COMPLETED, EnumSet.noneOf(WorkflowState.class));
        allowedTransitions.put(WorkflowState.FAILED, EnumSet.noneOf(WorkflowState.class));
        allowedTransitions.put(WorkflowState.CANCELLED, EnumSet.noneOf(WorkflowState.class));
    }

    public WorkflowState transition(WorkflowState current, WorkflowState target) {
        Set<WorkflowState> allowed = allowedTransitions.get(current);
        if (allowed == null || !allowed.contains(target)) {
            throw new ValidationException(String.format("Invalid state transition from %s to %s", current, target));
        }
        return target;
    }
}