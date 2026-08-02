package com.aiqaos.brain.maturity;

import com.aiqaos.brain.maturity.BrainMaturityReport.StageAssessment;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * BRAIN-1 (ADR-082): assesses the QA Brain's attained maturity stage from a set of present capability
 * keys, against the {@link BrainMaturityModel}. Maturity is <b>cumulative</b>: the attained stage is
 * the highest stage such that it and <b>every</b> earlier stage are fully satisfied — a gap in an early
 * stage caps the result even if a later stage's capabilities happen to be present (no skipping to
 * "autonomous"). Honest self-assessment, not a certification of autonomy.
 */
@Component
public class BrainStageAssessor {

    static final String ATTESTATION =
            "Self-assessment of the QA Brain's maturity over declared capabilities — not a claim of "
                    + "autonomy. The attained stage is the highest cumulative stage whose capabilities "
                    + "(and all earlier stages') are present.";

    private final BrainMaturityModel model;

    public BrainStageAssessor(BrainMaturityModel model) {
        this.model = model;
    }

    public BrainMaturityReport assess(Set<String> presentCapabilities) {
        Set<String> present = presentCapabilities != null ? presentCapabilities : Set.of();

        List<StageAssessment> assessments = new ArrayList<>();
        BrainMaturityStage attained = null;
        boolean chainBroken = false;

        for (BrainMaturityStage stage : model.stages()) {   // ordered 0 → 5
            Set<String> missing = new LinkedHashSet<>();
            for (String cap : model.requiredCapabilities(stage)) {
                if (!present.contains(cap)) {
                    missing.add(cap);
                }
            }
            boolean satisfied = missing.isEmpty();
            assessments.add(new StageAssessment(stage, satisfied, missing));

            if (!satisfied) {
                chainBroken = true;          // cumulative: nothing above this counts
            } else if (!chainBroken) {
                attained = stage;            // advance only while the chain from 0 is unbroken
            }
        }
        return new BrainMaturityReport(ATTESTATION, attained, assessments);
    }
}
