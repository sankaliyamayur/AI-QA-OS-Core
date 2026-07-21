package com.aiqaos.dashboard.controller;

import com.aiqaos.core.entity.TestCaseEntity;
import com.aiqaos.core.repository.TestCaseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
