package com.aiqaos.core.model;

import java.util.ArrayList;
import java.util.List;

public class GeneratedTestCaseSuite {
    private String suiteId;
    private List<TestCase> testCases = new ArrayList<>();

    public GeneratedTestCaseSuite() {}

    public String getSuiteId() { return suiteId; }
    public void setSuiteId(String suiteId) { this.suiteId = suiteId; }

    public List<TestCase> getTestCases() { return testCases; }
    public void setTestCases(List<TestCase> testCases) { this.testCases = testCases; }

    public static class TestCase {
        private String id;
        private String name;
        private String description;
        private List<String> steps = new ArrayList<>();
        private String expectedResult;
        private String priority;

        public TestCase() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getSteps() { return steps; }
        public void setSteps(List<String> steps) { this.steps = steps; }

        public String getExpectedResult() { return expectedResult; }
        public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }
}
