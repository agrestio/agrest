package io.agrest.id;

import io.agrest.meta.AgEntity;

import java.util.Map;

/**
 * @since 5.0
 */
public interface AgObjectId {

    static AgObjectId of(Object idValue) {
        return new SingleValueId(idValue);
    }

    static AgObjectId ofMap(Map<String, Object> idMap) {
        return new MultiValueId(idMap);
    }

    int size();

    /**
     * @return Part of this ID, identified by {@code attributeName}
     */
    Object get(String attributeName);

    Map<String, Object> asMap(AgEntity<?> entity);
}
