package io.agrest.runtime.processor.update;

import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.protocol.Exp;
import io.agrest.meta.AgAttribute;

class ByKeyObjectMapper<T> implements ObjectMapper<T> {

    private AgAttribute attribute;

    public ByKeyObjectMapper(AgAttribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public Object keyForObject(T object) {
        return attribute.getPropertyReader().value(object);
    }

    @Override
    public Object keyForUpdate(EntityUpdate<T> u) {
        return u.getValues().get(attribute.getName());
    }

    @Override
    public Exp expressionForKey(Object key) {
        // allowing nulls here
        return Exp.keyValue(attribute.getName(), "=", key);
    }
}
