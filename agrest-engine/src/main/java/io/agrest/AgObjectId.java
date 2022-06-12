package io.agrest;

import io.agrest.meta.AgEntity;

import java.util.Map;

/**
 * @since 1.24
 */
public interface AgObjectId {

    static AgObjectId of(Object idValue) {
        return new SimpleObjectId(idValue);
    }

    static AgObjectId ofMap(Map<String, Object> idMap) {
        return new CompoundObjectId(idMap);
    }

    int size();

    /**
     * @return Part of this ID, identified by {@code attributeName}
     */
    Object get(String attributeName);

    /**
     * @return Original ID value, that was used to create this wrapper ID
     * @deprecated since 5.0, as there should be no reason to unwrap the ID implementation
     */
    @Deprecated
    Object get();

    Map<String, Object> asMap(AgEntity<?> entity);
}
