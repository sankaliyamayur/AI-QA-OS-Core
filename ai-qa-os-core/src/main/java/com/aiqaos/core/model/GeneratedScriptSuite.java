package com.aiqaos.core.model;

import java.util.ArrayList;
import java.util.List;

public class GeneratedScriptSuite {
    private String suiteId;
    private List<AutomationScript> scripts = new ArrayList<>();

    public GeneratedScriptSuite() {}

    public String getSuiteId() { return suiteId; }
    public void setSuiteId(String suiteId) { this.suiteId = suiteId; }

    public List<AutomationScript> getScripts() { return scripts; }
    public void setScripts(List<AutomationScript> scripts) { this.scripts = scripts; }

    public static class AutomationScript {
        private String scriptId;
        private String testCaseId;
        private String targetPlatform;
        private String code;
        private String language;
        private String framework;

        public AutomationScript() {}

        public String getScriptId() { return scriptId; }
        public void setScriptId(String scriptId) { this.scriptId = scriptId; }

        public String getTestCaseId() { return testCaseId; }
        public void setTestCaseId(String testCaseId) { this.testCaseId = testCaseId; }

        public String getTargetPlatform() { return targetPlatform; }
        public void setTargetPlatform(String targetPlatform) { this.targetPlatform = targetPlatform; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public String getFramework() { return framework; }
        public void setFramework(String framework) { this.framework = framework; }
    }
}
