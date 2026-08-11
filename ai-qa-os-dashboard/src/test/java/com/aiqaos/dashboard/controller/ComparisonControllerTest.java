package com.aiqaos.dashboard.controller;

import com.aiqaos.observability.repository.LLMCostRepository;
import com.aiqaos.orchestration.entity.WorkflowExecutionEntity;
import com.aiqaos.orchestration.repository.WorkflowExecutionRepository;
import com.aiqaos.orchestration.service.WorkflowExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** See ExecutionHistoryControllerTest for why this wires a real service over mocked repos. */
@WebMvcTest(value = ComparisonController.class, excludeAutoConfiguration = {
    org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
    org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration.class
})
@Import(ComparisonControllerTest.TestConfig.class)
class ComparisonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkflowExecutionRepository workflowExecutionRepository;

    @MockitoBean
    private LLMCostRepository llmCostRepository;

    @MockitoBean
    private com.aiqaos.security.jwt.JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private com.aiqaos.security.rbac.UserRepository userRepository;

    @MockitoBean
    private com.aiqaos.security.monitoring.SecurityMetricsCollector securityMetricsCollector;

    @TestConfiguration
    static class TestConfig {
        @Bean
        WorkflowExecutionService workflowExecutionService(WorkflowExecutionRepository repo, LLMCostRepository costRepo) {
            return new WorkflowExecutionService(repo, costRepo);
        }
    }

    @Test
    void compareReturnsComparisonDto() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        WorkflowExecutionEntity a = new WorkflowExecutionEntity();
        a.setExecutionId(id1);
        a.setStatus("COMPLETED");
        a.setDuration(1000);
        a.setExecutionCost(0.25);

        WorkflowExecutionEntity b = new WorkflowExecutionEntity();
        b.setExecutionId(id2);
        b.setStatus("COMPLETED");
        b.setDuration(1500);
        b.setExecutionCost(0.50);

        when(workflowExecutionRepository.findByExecutionId(id1)).thenReturn(Optional.of(a));
        when(workflowExecutionRepository.findByExecutionId(id2)).thenReturn(Optional.of(b));

        mockMvc.perform(get("/api/dashboard/compare").param("id1", id1.toString()).param("id2", id2.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationDeltaMs").value(500))
                .andExpect(jsonPath("$.costDelta").value(0.25));
    }
}
