package io.agrest.runtime.processor.update;

import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.meta.AgAttribute;
import io.agrest.protocol.Exp;

/**
 * @since 5.0
 */
public class ByPropertyObjectMapper<T> implements ObjectMapper<T> {

    private final AgAttribute attribute;

    public ByPropertyObjectMapper(AgAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public Exp expressionForKey(Object key) {
        // allowing nulls here
        return Exp.equal(attribute.getName(), key);
    }

    @Override
    public Object keyForObject(T object) {
        return attribute.getDataReader().read(object);
    }

    @Override
    public Object keyForUpdate(EntityUpdate<T> update) {
        return update.getAttributes().get(attribute.getName());
    }
}
