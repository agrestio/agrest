package io.agrest.property;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 3.7
 */
public class PropertyIdReader implements IdReader {

    private final Map<String, PropertyReader> idReaders;

    public PropertyIdReader(Map<String, PropertyReader> idReaders) {
        this.idReaders = idReaders;
    }

    @Override
    public Map<String, Object> id(Object root) {
        return idReaders.isEmpty()
                ? Collections.emptyMap()
                : idMap(root);
    }

    private Map<String, Object> idMap(Object root) {
        Map<String, Object> idMap = new HashMap<>((int) (idReaders.size() / 0.75d) + 1);
        idReaders.forEach((k, v) -> idMap.put(k, v.value(root)));
        return idMap;
    }
}
