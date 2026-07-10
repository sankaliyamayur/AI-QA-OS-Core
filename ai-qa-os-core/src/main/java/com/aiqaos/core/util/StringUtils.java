package com.aiqaos.core.util;

public final class StringUtils {
    private StringUtils() {}
    public static boolean isBlank(String str) {
        return !org.springframework.util.StringUtils.hasText(str);
    }
    public static boolean isNotBlank(String str) {
        return org.springframework.util.StringUtils.hasText(str);
    }
}