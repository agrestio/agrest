package io.agrest.property;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 3.4
 */
public class DefaultIdReader implements IdReader {

    private Collection<String> idProperties;
    private PropertyReader idPartsReader;

    public DefaultIdReader(Collection<String> idProperties) {
        this.idProperties = idProperties;
        this.idPartsReader = BeanPropertyReader.reader();
    }

    @Override
    public Map<String, Object> id(Object root) {
        return idProperties.isEmpty()
                ? Collections.emptyMap()
                : idMap(root);
    }

    private Map<String, Object> idMap(Object root) {
        Map<String, Object> idMap = new HashMap<>((int) (idProperties.size() / 0.75d) + 1);
        for (String id : idProperties) {
            idMap.put(id, idPartsReader.value(root, id));
        }

        return idMap;
    }
}
