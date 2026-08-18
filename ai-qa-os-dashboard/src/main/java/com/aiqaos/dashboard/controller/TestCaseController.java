package com.aiqaos.dashboard.controller;

import com.aiqaos.core.entity.TestCaseEntity;
import com.aiqaos.core.repository.TestCaseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard/testcases")
public class TestCaseController {

    private final TestCaseRepository testCaseRepository;

    public TestCaseController(TestCaseRepository testCaseRepository) {
        this.testCaseRepository = testCaseRepository;
    }

    @GetMapping
    public ResponseEntity<List<TestCaseEntity>> getTestCases(@RequestParam(value = "moduleId", required = false) String moduleId) {
        if (moduleId != null && !moduleId.isEmpty()) {
            return ResponseEntity.ok(testCaseRepository.findByModuleId(moduleId));
        }
        return ResponseEntity.ok(testCaseRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestCaseEntity> getTestCaseById(@PathVariable("id") String id) {
        return testCaseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestCaseEntity> updateTestCase(@PathVariable("id") String id, @RequestBody TestCaseEntity updated) {
        return testCaseRepository.findById(id).map(tc -> {
            if (updated.getStatus() != null) tc.setStatus(updated.getStatus());
            if (updated.getFailureReason() != null) tc.setFailureReason(updated.getFailureReason());
            if (updated.getErrorMessage() != null) tc.setErrorMessage(updated.getErrorMessage());
            if (updated.getStackTrace() != null) tc.setStackTrace(updated.getStackTrace());
            if (updated.getSteps() != null) tc.setSteps(updated.getSteps());
            tc.setLastRun(java.time.LocalDateTime.now());
            testCaseRepository.save(tc);
            return ResponseEntity.ok(tc);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<TestCaseEntity> approveTestCase(@PathVariable("id") String id) {
        return testCaseRepository.findById(id).map(tc -> {
            tc.setStatus("Passed");
            tc.setFailureReason(null);
            tc.setErrorMessage(null);
            tc.setStackTrace(null);
            tc.setLastRun(java.time.LocalDateTime.now());
            if (tc.getSteps() != null) {
                for (java.util.Map<String, Object> step : tc.getSteps()) {
                    step.put("status", "Passed");
                    step.remove("error");
                }
            }
            testCaseRepository.save(tc);
            return ResponseEntity.ok(tc);
        }).orElse(ResponseEntity.notFound().build());
    }
}
