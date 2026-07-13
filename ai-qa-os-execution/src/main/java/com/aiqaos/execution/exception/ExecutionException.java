package com.aiqaos.execution.exception;

import com.aiqaos.core.exception.BaseException;
import com.aiqaos.core.exception.ErrorCode;

public class ExecutionException extends BaseException {
    public ExecutionException(String message) {
        super(ErrorCode.SYSTEM_ERROR, message);
    }
}