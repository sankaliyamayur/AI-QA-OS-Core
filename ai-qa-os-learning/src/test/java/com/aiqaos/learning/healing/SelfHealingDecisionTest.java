package com.aiqaos.learning.healing;

import com.aiqaos.core.enums.HealingActionType;
import com.aiqaos.core.model.BugAnalysisReport;
import com.aiqaos.core.model.QAExecutionReport;
import com.aiqaos.core.model.SelfHealingRecommendation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SelfHealingDecisionTest {

    @Test
    void testLocatorHealingActionGeneration() {
        SelfHealingEngine engine = new SelfHealingEngine();

        QAExecutionReport report = new QAExecutionReport();
        report.setOverallResult("FAIL");
        BugAnalysisReport bug = new BugAnalysisReport();
        bug.setFailureCategory("ELEMENT_NOT_FOUND");
        bug.setRootCause("Selector #submitBtn not matched");
        bug.setSelfHealingSuggestion("Change selector to button[type='submit']");
        bug.setConfidence(0.92);
        report.setBugAnalysisReport(bug);

        List<SelfHealingRecommendation> recommendations = engine.generateRecommendations(report);

        assertEquals(1, recommendations.size());
        SelfHealingRecommendation rec = recommendations.get(0);
        assertEquals(HealingActionType.LOCATOR_UPDATE, rec.getActionType());
        assertTrue(rec.isUpdateLocator());
        assertFalse(rec.isRegenerateScript());
        assertEquals("Change selector to button[type='submit']", rec.getSuggestedAction());
    }

    @Test
    void testRegenerationActionTrigger() {
        SelfHealingEngine engine = new SelfHealingEngine();

        QAExecutionReport report = new QAExecutionReport();
        report.setOverallResult("FAIL");
        BugAnalysisReport bug = new BugAnalysisReport();
        bug.setFailureCategory("SCRIPT_SYNTAX_ERROR");
        bug.setRootCause("Missing parenthesis in generated script");
        bug.setSelfHealingSuggestion("Re-generate code structure");
        bug.setRequiresRegeneration(true);
        bug.setConfidence(0.88);
        report.setBugAnalysisReport(bug);

        List<SelfHealingRecommendation> recommendations = engine.generateRecommendations(report);

        assertEquals(1, recommendations.size());
        SelfHealingRecommendation rec = recommendations.get(0);
        assertEquals(HealingActionType.SCRIPT_REGENERATE, rec.getActionType());
        assertTrue(rec.isRegenerateScript());
    }
}
