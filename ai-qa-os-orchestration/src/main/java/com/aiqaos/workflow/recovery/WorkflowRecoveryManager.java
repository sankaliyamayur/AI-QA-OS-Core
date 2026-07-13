package com.aiqaos.workflow.recovery;

import com.aiqaos.core.context.WorkflowContext;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class WorkflowRecoveryManager {
    private final List<CompensationAction> compensations = new ArrayList<>();

    public void registerCompensation(CompensationAction action) {
        compensations.add(action);
    }

    public void runRollbacks(WorkflowContext context) {
        // Execute in reverse order
        for (int i = compensations.size() - 1; i >= 0; i--) {
            try {
                compensations.get(i).rollback(context);
            } catch (Exception e) {
                // Suppressed to ensure downstream rollback completion
            }
        }
    }
}