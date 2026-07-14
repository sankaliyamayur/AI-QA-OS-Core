package com.aiqaos.learning.healing;

import com.aiqaos.core.enums.HealingActionType;
import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.QAExecutionReport;
import com.aiqaos.core.model.SelfHealingRecommendation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SelfHealingEngine {

    public List<SelfHealingRecommendation> generateRecommendations(QAExecutionReport report) {
        List<SelfHealingRecommendation> recommendations = new ArrayList<>();

        BugAnalysisReport bugReport = report.getBugAnalysisReport();
        if (bugReport != null && "FAIL".equals(report.getOverallResult())) {
            SelfHealingRecommendation rec = new SelfHealingRecommendation();
            rec.setRecommendationId("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            rec.setIssue(bugReport.getRootCause() != null ? bugReport.getRootCause() : "Unknown execution failure");
            rec.setSuggestedAction(bugReport.getSelfHealingSuggestion() != null 
                    ? bugReport.getSelfHealingSuggestion() 
                    : "Review execution logs");
            rec.setConfidence(bugReport.getConfidence() > 0 ? bugReport.getConfidence() : 0.80);

            // Mapping heuristic to HealingActionType and updates
            String category = bugReport.getFailureCategory() != null ? bugReport.getFailureCategory() : "";
            if (category.contains("ELEMENT") || category.contains("LOCATOR") || category.contains("SELECTOR")) {
                rec.setActionType(HealingActionType.LOCATOR_UPDATE);
                rec.setUpdateLocator(true);
            } else if (bugReport.isRequiresRegeneration()) {
                rec.setActionType(HealingActionType.SCRIPT_REGENERATE);
                rec.setRegenerateScript(true);
            } else if (category.contains("TIMEOUT")) {
                rec.setActionType(HealingActionType.WAIT_STRATEGY_CHANGE);
            } else {
                rec.setActionType(HealingActionType.PROMPT_UPDATE);
                rec.setUpdatePrompt(true);
            }

            recommendations.add(rec);
        }

        return recommendations;
    }
}
