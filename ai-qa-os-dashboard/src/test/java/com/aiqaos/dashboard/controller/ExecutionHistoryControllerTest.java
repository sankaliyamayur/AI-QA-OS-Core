package com.aiqaos.dashboard.controller;

import com.aiqaos.observability.repository.LLMCostRepository;
import com.aiqaos.workflow.entity.WorkflowExecutionEntity;
import com.aiqaos.workflow.repository.WorkflowExecutionRepository;
import com.aiqaos.workflow.service.WorkflowExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Backs the controller with a REAL WorkflowExecutionService wired to mocked *interface*
 * repositories - mocking WorkflowExecutionService itself (a concrete class) hits a
 * Mockito/ByteBuddy limitation under newer JDKs, whereas interface mocks (JDK proxies) don't.
 */
@WebMvcTest(value = ExecutionHistoryController.class, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
@Import(ExecutionHistoryControllerTest.TestConfig.class)
class ExecutionHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowExecutionRepository workflowExecutionRepository;

    @MockBean
    private LLMCostRepository llmCostRepository;

    @MockBean
    private com.aiqaos.security.jwt.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.aiqaos.security.rbac.UserRepository userRepository;

    @MockBean
    private com.aiqaos.security.monitoring.SecurityMetricsCollector securityMetricsCollector;

    @TestConfiguration
    static class TestConfig {
        @Bean
        WorkflowExecutionService workflowExecutionService(WorkflowExecutionRepository repo, LLMCostRepository costRepo) {
            return new WorkflowExecutionService(repo, costRepo);
        }
    }

    @Test
    void searchReturnsPagedExecutions() throws Exception {
        WorkflowExecutionEntity entity = new WorkflowExecutionEntity();
        entity.setExecutionId(UUID.randomUUID());
        entity.setWorkflowId(UUID.randomUUID());
        entity.setStatus("COMPLETED");

        when(workflowExecutionRepository.search(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(entity)));

        mockMvc.perform(get("/api/dashboard/executions").param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("COMPLETED"));
    }

    @Test
    void getByExecutionIdReturns404WhenMissing() throws Exception {
        UUID executionId = UUID.randomUUID();
        when(workflowExecutionRepository.findByExecutionId(executionId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/dashboard/executions/" + executionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByExecutionIdReturnsDtoWhenPresent() throws Exception {
        UUID executionId = UUID.randomUUID();
        WorkflowExecutionEntity entity = new WorkflowExecutionEntity();
        entity.setExecutionId(executionId);
        entity.setStatus("RUNNING");
        when(workflowExecutionRepository.findByExecutionId(executionId)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/dashboard/executions/" + executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }
}
