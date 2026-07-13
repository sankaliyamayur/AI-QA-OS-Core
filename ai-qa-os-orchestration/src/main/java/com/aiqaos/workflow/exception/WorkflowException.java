package com.aiqaos.workflow.exception;

import com.aiqaos.core.exception.BaseException;
import com.aiqaos.core.exception.ErrorCode;

public class WorkflowException extends BaseException {
    public WorkflowException(String message) {
        super(ErrorCode.SYSTEM_ERROR, message);
    }
}