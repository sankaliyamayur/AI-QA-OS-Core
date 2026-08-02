package com.aiqaos.brain.maturity;

import static com.aiqaos.brain.maturity.BrainMaturityModel.CAP_AUTONOMOUS_GOVERNANCE;
import static com.aiqaos.brain.maturity.BrainMaturityModel.CAP_CONFIDENCE_GATE;
import static com.aiqaos.brain.maturity.BrainMaturityModel.CAP_FEEDBACK;
import static com.aiqaos.brain.maturity.BrainMaturityModel.CAP_HEALING_APPROVAL;
import static com.aiqaos.brain.maturity.BrainMaturityModel.CAP_HYBRID;
import static com.aiqaos.brain.maturity.BrainMaturityModel.CAP_LEARNING;
import static com.aiqaos.brain.maturity.BrainMaturityModel.CAP_LLM;
import static com.aiqaos.brain.maturity.BrainMaturityModel.CAP_QA_PLANNER;
import static com.aiqaos.brain.maturity.BrainMaturityModel.CAP_RULE_BASED;
import static com.aiqaos.brain.maturity.BrainMaturityModel.CAP_TEST_STRATEGY_PLANNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aiqaos.brain.maturity.BrainMaturityReport.StageAssessment;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** BRAIN-1 (ADR-082): cumulative attained-stage assessment over declared capabilities. */
class BrainStageAssessorTest {

    private final BrainStageAssessor assessor = new BrainStageAssessor(new BrainMaturityModel());

    private static final Set<String> ALL = Set.of(
            CAP_RULE_BASED, CAP_LLM, CAP_CONFIDENCE_GATE, CAP_HYBRID, CAP_HEALING_APPROVAL,
            CAP_LEARNING, CAP_FEEDBACK, CAP_QA_PLANNER, CAP_TEST_STRATEGY_PLANNER, CAP_AUTONOMOUS_GOVERNANCE);

    @Test
    void fullCapabilities_attainAutonomous() {
        assertEquals(BrainMaturityStage.AUTONOMOUS, assessor.assess(ALL).attainedStage());
    }

    @Test
    void onlyRuleBased_attainAssisted() {
        assertEquals(BrainMaturityStage.ASSISTED, assessor.assess(Set.of(CAP_RULE_BASED)).attainedStage());
    }

    @Test
    void emptyCapabilities_attainNothing() {
        assertNull(assessor.assess(Set.of()).attainedStage());
        assertNull(assessor.assess(null).attainedStage());
    }

    @Test
    void gapInEarlyStage_capsAttainedDespiteLaterCapabilities() {
        // Rule-based + both planners present, but no ADVISORY capabilities -> chain breaks at ADVISORY.
        BrainMaturityReport report = assessor.assess(Set.of(
                CAP_RULE_BASED, CAP_QA_PLANNER, CAP_TEST_STRATEGY_PLANNER));

        assertEquals(BrainMaturityStage.ASSISTED, report.attainedStage(), "cumulative: cannot skip ADVISORY");
        assertTrue(stage(report, BrainMaturityStage.ORCHESTRATED).satisfied(), "planners present -> stage satisfied");
        assertFalse(stage(report, BrainMaturityStage.ADVISORY).satisfied(), "advisory unmet breaks the chain");
    }

    @Test
    void reportsMissingCapabilitiesPerStage() {
        BrainMaturityReport report = assessor.assess(Set.of(CAP_RULE_BASED));
        assertTrue(stage(report, BrainMaturityStage.ADVISORY).missing().contains(CAP_LLM));
        assertTrue(stage(report, BrainMaturityStage.ADVISORY).missing().contains(CAP_CONFIDENCE_GATE));
    }

    @Test
    void assessmentCoversAllSixStagesInOrder() {
        var stages = assessor.assess(Set.of()).stages();
        assertEquals(6, stages.size());
        assertEquals(BrainMaturityStage.ASSISTED, stages.get(0).stage());
        assertEquals(BrainMaturityStage.AUTONOMOUS, stages.get(5).stage());
    }

    private static StageAssessment stage(BrainMaturityReport report, BrainMaturityStage s) {
        return report.stages().stream().filter(a -> a.stage() == s).findFirst().orElseThrow();
    }
}
