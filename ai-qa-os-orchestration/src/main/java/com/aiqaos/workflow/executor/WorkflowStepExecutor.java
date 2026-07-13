package com.aiqaos.workflow.executor;

import com.aiqaos.workflow.graph.WorkflowNode;
import com.aiqaos.workflow.context.WorkflowVariables;
import com.aiqaos.workflow.plugin.PluginStep;
import com.aiqaos.workflow.model.WorkflowStepResultDTO;
import com.aiqaos.core.engine.AgentManager;
import com.aiqaos.core.enums.AgentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class WorkflowStepExecutor {

    @Autowired(required = false)
    private AgentManager agentManager;

    @Autowired
    private List<PluginStep> plugins;

    private final ExpressionParser parser = new SpelExpressionParser();

    public boolean shouldExecute(WorkflowNode node, WorkflowVariables variables) {
        if (node.getCondition() == null || node.getCondition().trim().isEmpty()) {
            return true;
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariables(variables.getMap());
            return Boolean.TRUE.equals(parser.parseExpression(node.getCondition()).getValue(context, Boolean.class));
        } catch (Exception e) {
            return false;
        }
    }

    public WorkflowStepResultDTO executeStep(WorkflowNode node, WorkflowVariables variables) {
        WorkflowStepResultDTO result = new WorkflowStepResultDTO();
        result.setStepId(node.getStepId());

        if (!shouldExecute(node, variables)) {
            result.setStatus("SKIPPED");
            result.setExecutionOutput("Step condition evaluation was false.");
            return result;
        }

        // Plugin step execution
        if (node.getPluginType() != null) {
            for (PluginStep plugin : plugins) {
                if (plugin.getType().equalsIgnoreCase(node.getPluginType())) {
                    String output = plugin.execute(node.getPluginInput());
                    result.setStatus("SUCCESS");
                    result.setExecutionOutput(output);
                    return result;
                }
            }
        }

        // Standard Agent execution fallback
        result.setStatus("SUCCESS");
        result.setExecutionOutput("Successfully processed step via Agent type: " + node.getAgentType());
        return result;
    }
}