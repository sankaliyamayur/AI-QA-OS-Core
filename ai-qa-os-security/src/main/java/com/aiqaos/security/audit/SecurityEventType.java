package com.aiqaos.security.audit;

public enum SecurityEventType {
    LOGIN,
    LOGIN_FAILED,
    TOKEN_REFRESH,
    PASSWORD_CHANGED,
    ROLE_CHANGED,
    PERMISSION_CHANGED,
    LOGOUT,
    ACCOUNT_LOCKED,
    UNAUTHORIZED,
    ACCESS_DENIED
}
