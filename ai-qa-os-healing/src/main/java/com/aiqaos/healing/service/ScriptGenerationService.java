package com.aiqaos.healing.service;

import com.aiqaos.core.model.GeneratedScriptSuite;
import com.aiqaos.core.model.QAExecutionReport;

public interface ScriptGenerationService {
    GeneratedScriptSuite regenerate(QAExecutionReport report, String reason);
}
