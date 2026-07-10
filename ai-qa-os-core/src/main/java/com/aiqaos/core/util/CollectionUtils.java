package com.aiqaos.core.util;

import java.util.Collection;

public final class CollectionUtils {
    private CollectionUtils() {}

    public static boolean isEmpty(Collection<?> collection) {
        return org.springframework.util.CollectionUtils.isEmpty(collection);
    }
}