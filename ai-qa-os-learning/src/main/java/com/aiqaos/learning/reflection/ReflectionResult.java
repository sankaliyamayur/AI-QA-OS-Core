package com.aiqaos.learning.reflection;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * LRN-1: the outcome of reflecting over failure patterns — the improvement proposals produced, with
 * counts by {@link ProposalType} for a quick summary.
 */
public class ReflectionResult {

    private final List<ImprovementProposal> proposals;

    public ReflectionResult() {
        this.proposals = new ArrayList<>();
    }

    public ReflectionResult(List<ImprovementProposal> proposals) {
        this.proposals = proposals != null ? new ArrayList<>(proposals) : new ArrayList<>();
    }

    public List<ImprovementProposal> getProposals() {
        return proposals;
    }

    public int getCount() {
        return proposals.size();
    }

    /** Proposal counts per type (types with zero are omitted). */
    public Map<ProposalType, Integer> countsByType() {
        Map<ProposalType, Integer> counts = new EnumMap<>(ProposalType.class);
        for (ImprovementProposal p : proposals) {
            counts.merge(p.getType(), 1, Integer::sum);
        }
        return counts;
    }

    public String summary() {
        return getCount() + " improvement proposal(s): " + countsByType();
    }
}
