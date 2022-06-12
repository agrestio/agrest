package io.agrest;

import io.agrest.converter.jsonvalue.Normalizer;
import io.agrest.meta.AgIdPart;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * A single value id.
 *
 * @since 1.24
 */
public class SimpleObjectId extends BaseObjectId {

    private final Object id;

    protected SimpleObjectId(Object id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public Object get(String attributeName) {
        // TODO: check for valid attribute name?
        return id;
    }

    @Deprecated
    @Override
    public Object get() {
        return id;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    protected Map<String, Object> asMap(Collection<AgIdPart> idAttributes) {
        AgIdPart idAttribute = idAttributes.iterator().next();
        return Collections.singletonMap(idAttribute.getName(), Normalizer.normalize(id, idAttribute.getType()));
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof SimpleObjectId)) {
            return false;
        }

        return Objects.equals(id, ((SimpleObjectId) object).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
