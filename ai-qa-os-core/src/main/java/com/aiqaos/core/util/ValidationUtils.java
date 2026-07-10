package com.aiqaos.core.util;

import com.aiqaos.core.exception.ValidationException;

public final class ValidationUtils {
    private ValidationUtils() {}

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new ValidationException(message, null);
        }
    }
}