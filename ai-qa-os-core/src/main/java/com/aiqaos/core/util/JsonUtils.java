package com.aiqaos.core.util;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public final class JsonUtils {
    private JsonUtils() {}
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
}