package io.agrest.runtime.processor.update;

import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.meta.AgAttribute;
import io.agrest.protocol.Exp;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @since 5.0
 */
public class ByPropertiesObjectMapper<T> implements ObjectMapper<T> {

    private final AgAttribute[] attributes;
    private final int keyMapCapacity;

    public ByPropertiesObjectMapper(AgAttribute[] attributes) {
        this.attributes = attributes;
        this.keyMapCapacity = 1 + (int) (attributes.length / 0.75);
    }

    @Override
    public Exp expressionForKey(Object key) {

        // allowing null values in the map, but not the null key map
        Map<String, Object> map = (Map<String, Object>) Objects.requireNonNull(key);

        int len = attributes.length;
        Exp[] exps = new Exp[len];

        for (int i = 0; i < len; i++) {
            String n = attributes[i].getName();
            exps[i] = Exp.equal(n, map.get(n));
        }

        return Exp.and(exps);
    }

    @Override
    public Object keyForObject(T object) {
        Map<String, Object> key = new HashMap<>(keyMapCapacity);
        for (AgAttribute a : attributes) {
            key.put(a.getName(), a.getDataReader().read(object));
        }
        return key;
    }

    @Override
    public Object keyForUpdate(EntityUpdate<T> update) {
        Map<String, Object> key = new HashMap<>(keyMapCapacity);
        for (AgAttribute a : attributes) {
            String n = a.getName();
            key.put(n, update.getAttribute(n));
        }

        return key;
    }
}
