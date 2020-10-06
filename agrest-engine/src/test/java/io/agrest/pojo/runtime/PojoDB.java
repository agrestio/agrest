package io.agrest.pojo.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A key/value "database" that stores objects by type and id.
 */
public class PojoDB {

    private ConcurrentMap<Class<?>, Map<Object, Object>> map;

    public PojoDB() {
        this.map = new ConcurrentHashMap<>();
    }

    public void clear() {
        map.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> Map<Object, T> bucketForType(Class<T> type) {
        return (Map<Object, T>) map.computeIfAbsent(type, t -> new ConcurrentHashMap<>());
    }
}
