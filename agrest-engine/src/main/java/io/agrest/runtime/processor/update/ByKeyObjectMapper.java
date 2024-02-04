package io.agrest.runtime.processor.update;

import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.protocol.Exp;
import io.agrest.meta.AgAttribute;

import java.util.Objects;

class ByKeyObjectMapper<T> implements ObjectMapper<T> {

    private final AgAttribute attribute;

    public ByKeyObjectMapper(AgAttribute attribute) {
        this.attribute = Objects.requireNonNull(attribute);
    }

    @Override
    public Object keyForObject(T object) {
        return attribute.getDataReader().read(object);
    }

    @Override
    public Object keyForUpdate(EntityUpdate<T> u) {
        return u.getAttributes().get(attribute.getName());
    }

    @Override
    public Exp expressionForKey(Object key) {
        // allowing nulls here
        return Exp.equal(attribute.getName(), key);
    }
}
