package io.agrest;

import io.agrest.meta.LrEntity;

import java.util.Map;

/**
 * @since 1.24
 */
public interface LrObjectId {

    int size();

    /**
     * @return Part of this ID, identified by {@code attributeName}
     */
    Object get(String attributeName);

    /**
     * @return Original ID value, that was used to create this wrapper ID
     */
    Object get();

    Map<String, Object> asMap(LrEntity<?> entity);
}
