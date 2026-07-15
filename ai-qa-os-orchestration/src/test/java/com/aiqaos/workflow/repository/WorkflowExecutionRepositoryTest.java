package com.aiqaos.workflow.repository;

import com.aiqaos.workflow.entity.WorkflowExecutionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class WorkflowExecutionRepositoryTest {

    @Autowired
    private WorkflowExecutionRepository repository;

    @Test
    void findersAndSearchFilterCorrectly() {
        UUID executionId1 = UUID.randomUUID();
        UUID executionId2 = UUID.randomUUID();

        WorkflowExecutionEntity a = new WorkflowExecutionEntity();
        a.setExecutionId(executionId1);
        a.setWorkflowId(UUID.randomUUID());
        a.setStatus("COMPLETED");
        a.setGitBranch("main");
        a.setStartTime(LocalDateTime.now().minusHours(2));
        a.setDuration(500);
        repository.save(a);

        WorkflowExecutionEntity b = new WorkflowExecutionEntity();
        b.setExecutionId(executionId2);
        b.setWorkflowId(UUID.randomUUID());
        b.setStatus("FAILED");
        b.setGitBranch("feature/x");
        b.setStartTime(LocalDateTime.now().minusHours(1));
        b.setDuration(1500);
        repository.save(b);

        assertTrue(repository.findByExecutionId(executionId1).isPresent());
        assertEquals("COMPLETED", repository.findByExecutionId(executionId1).get().getStatus());

        assertEquals(1, repository.findByStatus("FAILED").size());
        assertEquals(1, repository.findByGitBranch("main").size());
        assertEquals(2, repository.findByDurationBetween(400, 2000).size());
        assertEquals(1, repository.findByDurationBetween(1000, 2000).size());

        List<WorkflowExecutionEntity> recent = repository.findTop50ByOrderByStartTimeDesc();
        assertEquals(executionId2, recent.get(0).getExecutionId());

        var page = repository.search("COMPLETED", null, null, null, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("COMPLETED", page.getContent().get(0).getStatus());
    }
}
