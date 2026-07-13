package com.aiqaos.agent.exception;

import com.aiqaos.core.exception.BaseException;
import com.aiqaos.core.exception.ErrorCode;

public class AgentException extends BaseException {
    public AgentException(String message) {
        super(ErrorCode.SYSTEM_ERROR, message);
    }
}