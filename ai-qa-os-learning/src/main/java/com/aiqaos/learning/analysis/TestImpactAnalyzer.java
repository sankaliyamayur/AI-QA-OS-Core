package com.aiqaos.learning.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TestImpactAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(TestImpactAnalyzer.class);

    private final Map<String, List<String>> componentToTestMapping = new HashMap<>();

    public TestImpactAnalyzer() {
        // Default mappings based on component domain naming conventions
        componentToTestMapping.put("auth", List.of("TEST_CASE_GENERATOR", "SCRIPT_GENERATOR", "EXECUTION_ENGINEER"));
        componentToTestMapping.put("login", List.of("TEST_CASE_GENERATOR", "SCRIPT_GENERATOR", "EXECUTION_ENGINEER"));
        componentToTestMapping.put("checkout", List.of("TEST_CASE_GENERATOR", "SCRIPT_GENERATOR", "EXECUTION_ENGINEER"));
        componentToTestMapping.put("reporting", List.of("REPORTING"));
        componentToTestMapping.put("healing", List.of("SELF_HEALING"));
    }

    public List<String> analyzeImpactedSteps(List<String> modifiedFiles, List<String> allAvailableSteps) {
        if (modifiedFiles == null || modifiedFiles.isEmpty()) {
            log.info("WF-3: No modified files provided. Returning full test suite.");
            return allAvailableSteps != null ? allAvailableSteps : Collections.emptyList();
        }

        Set<String> selectedSteps = new HashSet<>();

        for (String file : modifiedFiles) {
            String lower = file.toLowerCase();
            for (Map.Entry<String, List<String>> entry : componentToTestMapping.entrySet()) {
                if (lower.contains(entry.getKey())) {
                    selectedSteps.addAll(entry.getValue());
                }
            }
        }

        // If no specific component match found, fall back to executing all steps
        if (selectedSteps.isEmpty() && allAvailableSteps != null) {
            log.info("WF-3: Impact analysis match empty; defaulting to all steps.");
            return allAvailableSteps;
        }

        List<String> result = allAvailableSteps != null ?
                allAvailableSteps.stream().filter(selectedSteps::contains).collect(Collectors.toList()) :
                new ArrayList<>(selectedSteps);

        log.info("WF-3: Test Impact Analysis selected {} steps for modified files {}: {}",
                result.size(), modifiedFiles, result);

        return result;
    }
}
