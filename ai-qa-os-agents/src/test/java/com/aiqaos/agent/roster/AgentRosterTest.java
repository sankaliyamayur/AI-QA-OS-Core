package com.aiqaos.agent.roster;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

/** AGT-1: unit tests for the seeded agent roster catalog. No Mockito. */
class AgentRosterTest {

    private final AgentRoster roster = new AgentRoster();

    @Test
    void rosterHasTheEighteenDesignedAgents() {
        assertThat(roster.total()).isEqualTo(18);
    }

    @Test
    void statusCountsMatchTheRoadmap() {
        assertThat(roster.byStatus(AgentStatus.IMPLEMENTED)).hasSize(6);
        assertThat(roster.byStatus(AgentStatus.DESIGNED)).hasSize(2);
        assertThat(roster.byStatus(AgentStatus.FUTURE)).hasSize(10);
    }

    @Test
    void nonFunctionalCategoryHasTheFourSpecialistAgents() {
        assertThat(roster.byCategory(AgentCategory.NON_FUNCTIONAL))
                .extracting(AgentDescriptor::getName)
                .containsExactlyInAnyOrder("Performance Agent", "Security Agent",
                        "Accessibility Agent", "Visual Testing Agent");
    }

    @Test
    void implementedAgentsCarryTheirImplementingClass() {
        assertThat(roster.find("Requirement Agent")).isPresent().get()
                .satisfies(d -> {
                    assertThat(d.isImplemented()).isTrue();
                    assertThat(d.getImplementingClass()).isEqualTo("QAAnalystAgent");
                });
    }

    @Test
    void futureAgentIsCatalogedWithoutAnImpl() {
        assertThat(roster.find("API Agent")).isPresent().get()
                .satisfies(d -> {
                    assertThat(d.getStatus()).isEqualTo(AgentStatus.FUTURE);
                    assertThat(d.getImplementingClass()).isNull();
                });
    }

    @Test
    void coverageIsSixOfEighteen() {
        assertThat(roster.implementedCount()).isEqualTo(6);
        assertThat(roster.coverageRatio()).isCloseTo(6.0 / 18.0, within(1e-9));
    }

    @Test
    void allEightCategoriesArePresent() {
        assertThat(roster.categories()).hasSize(8);
    }
}
