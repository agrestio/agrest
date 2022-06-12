package io.agrest.id;

import io.agrest.converter.jsonvalue.Normalizer;
import io.agrest.meta.AgIdPart;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * A single value id.
 *
 * @since 5.0
 */
public class SingleValueId extends BaseObjectId {

    private final Object id;

    protected SingleValueId(Object id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public Object get(String attributeName) {
        // TODO: check for valid attribute name?
        return id;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    protected Map<String, Object> asMap(Collection<AgIdPart> idAttributes) {
        AgIdPart idAttribute = idAttributes.iterator().next();
        return Map.of(idAttribute.getName(), Normalizer.normalize(id, idAttribute.getType()));
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

        if (!(object instanceof SingleValueId)) {
            return false;
        }

        return Objects.equals(id, ((SingleValueId) object).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
