package com.aiqaos.core.util;

import java.util.UUID;

public final class IdGenerator {
    private IdGenerator() {}

    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }
}